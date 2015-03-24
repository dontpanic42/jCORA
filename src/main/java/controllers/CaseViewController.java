package controllers;

import com.sun.glass.ui.Application;
import controllers.commons.WaitViewController;
import controllers.dataproperty.DataPropertyEditorFactory;
import controllers.queryeditor.QueryViewController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import models.datatypes.TypedValue;
import models.ontology.CoraDataPropertyModel;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;
import models.ontology.assertions.DataPropertyAssertion;
import models.ontology.assertions.ObjectPropertyAssertion;
import services.adaption.utility.PartialCaseCopier;
import services.rules.jenarules.JenaRulesEngine;
import view.Commons;
import view.graphview.GraphViewComponent;
import view.viewbuilder.ViewBuilder;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Controller für die Fallansicht
 *
 * Created by daniel on 24.08.14.
 */
public class CaseViewController implements CoraCaseModel.CaseChangeHandler {

    private static final String QUERY_VIEW_VIEW_FILE = "views/queryeditor/queryView.fxml";

    @FXML
    private SwingNode swingNode;

    @FXML
    private SwingNode navNode;

    /**
     * Spalte für Attributsnamen
     */
    @FXML
    private TableColumn<DataPropertyAssertion, CoraDataPropertyModel> columnPropertyName;

    /**
     * Spalte für Attributswerte
     */
    @FXML
    private TableColumn<DataPropertyAssertion, String>  columnPropertyValue;

    /**
     * Spalte für Datentyp/Einheit des Werts
     */
    @FXML
    private TableColumn<DataPropertyAssertion, String> columnPropertyUnit;

    /**
     * Attributstabelle
     */
    @FXML
    private TableView<DataPropertyAssertion> tblDataProperties;

    /**
     * "Speichern"-Button. Ist nur aktiviert, wenn der Fall bereits eine FallID besitzt,
     * ähnlich der Unterscheidung "Speichern" und "Speichern unter"
     */
    @FXML
    private Button btnSave;

    /**
     * Textfeld für die Suche nach Instanzen im Fallgraphen
     */
    @FXML
    private TextField searchTextField;

    /**
     * Ergebnisse der Suche nach Instanzen im Fallgraphen
     */
    @FXML
    private ListView<CoraInstanceModel> searchListView;

    /**
     * Label, das die derzeit ausgewählte Instanz anzeigt, über der Attributstabelle.
     * Inhalt: "Attribut: [Instanzname]"
     */
    @FXML
    private Label lblSelectionName;

    /**
     * Der Fallgraph
     */
    private final GraphViewComponent graph = new GraphViewComponent();

    /**
     * Die Instanz, die den Ausgangspunkt für den angezeigten Fall darstellt.
     * Wird ein vollständiger Fall angezeigt eine Instanz von "Fall",
     * wird ein CBR-Request angzeieigt eine Instanz von "Fallbeschreibung".
     */
    private CoraInstanceModel model;

    /**
     * Die aktuell ausgewählte Instanz, oder <code>null</code>, wenn keine Instanz
     * ausgewählt ist.
     */
    private SimpleObjectProperty<CoraInstanceModel> currentSelection = new SimpleObjectProperty<>();

    /**
     * Asynchroner <code>Task</code>, der die Attribute (DataProperties) der ausgewählten Instanz anzeigt
     */
    private Task<ObservableList<DataPropertyAssertion>> showDataPropertiesTask;

    /**
     * Die Stage, in der der Fall angzeigt wird (für Modals)
     */
    private Stage stage;

    @FXML
    public void initialize() {
        createAndSetSwingContent(swingNode, navNode);

        setupShowSelection();

        setupEventHandlers();

        setupDataPropertyView();

        setupSearch();

        btnSave.setDisable(true);
    }

    /**
     * Setup-Methode für die Attribute-Tabelle
     */
    private void setupDataPropertyView() {
        tblDataProperties.setPlaceholder(new Label(ViewBuilder.getInstance().getText("ui.case_view.label_no_data_properties")));

        columnPropertyName.setCellValueFactory(
                p1 -> new ReadOnlyObjectWrapper<>(p1.getValue().getPredicat()));

        columnPropertyName.setCellFactory(new Callback<TableColumn<DataPropertyAssertion, CoraDataPropertyModel>,
                TableCell<DataPropertyAssertion, CoraDataPropertyModel>>() {
            @Override
            public TableCell<DataPropertyAssertion, CoraDataPropertyModel> call(TableColumn<DataPropertyAssertion,
                    CoraDataPropertyModel> dataPropertyAssertionCoraDataPropertyModelTableColumn) {
                return new TableCell<DataPropertyAssertion, CoraDataPropertyModel>() {
                    @Override
                    protected void updateItem(CoraDataPropertyModel coraDataPropertyModel, boolean empty) {
                        super.updateItem(coraDataPropertyModel, empty);

                        if(empty) {
                            setText("");
                        } else {
                            final String lang = MainApplication.getInstance().getLanguage();
                            setText(coraDataPropertyModel.getDisplayName(lang));
                        }
                    }
                };
            }
        });

        columnPropertyValue.setCellValueFactory(
                p1 -> {
                    TypedValue object = p1.getValue().getObject();
                    if(object == null) {
                        return new ReadOnlyObjectWrapper<>("[null]");
                    } else {
                        return new ReadOnlyObjectWrapper<>(object.getAsString());
                    }
                });

        columnPropertyUnit.setCellValueFactory( (p) -> {
            TypedValue object = p.getValue().getObject();
            if(object == null) {
                return new ReadOnlyObjectWrapper<>("Unbek. Einheit");
            } else {
                return new ReadOnlyObjectWrapper<>(object.getUnitName());
            }
        });
    }

    /**
     * Setup-Methode für Fallgraph-Eventhandler
     */
    private void setupEventHandlers() {
        //Setup Evenent-Handlers
        graph.selectionProperty().addListener((ov, oldSelection, newSelection) ->
                onChangeSelection(newSelection));
        graph.setOnCreateRelation((ev) -> AddObjectPropertyViewController.showAddRelation(stage, ev.getParentInstance()));

        graph.setOnDeleteRelation((GraphViewComponent.DeleteRelationEvent ev) -> Application.invokeLater(() -> {
            removeObjectRelation(ev.getSubject(), ev.getPredicat(), ev.getObject());
        }));

        graph.setOnDeleteRelationRecursive((GraphViewComponent.DeleteRelationRecursiveEvent ev) -> Application.invokeLater(() -> {
            removeObjectRelationRecursive(ev.getSubject(), ev.getPredicat(), ev.getObject());
        }));
    }

    /**
     * Setup-Methode für die Anzeige der aktuell ausgewählten Instanz
     */
    private void setupShowSelection() {
        //Zeige die aktuelle Auswahl in einem Label an
        currentSelection.addListener((ov, oldValue, newValue) -> {
            Application.invokeLater(() -> {
                final ViewBuilder vb = ViewBuilder.getInstance();
                if(newValue == null) {
                    final String noSelection = vb.getText("ui.case_view.label_datatype_property_no_selection");
                    lblSelectionName.setText(noSelection);
                } else {
                    final String lang = MainApplication.getInstance().getLanguage();
                    final String name = newValue.getDisplayName(lang);
                    lblSelectionName.setText(name);
                }
            });
        });
    }

    /**
     * Installiert die Event-Handler für die Instanz-Suche
     */
    private void setupSearch() {
        /*
         * Wenn eine Instanz ausgewählt wird, zeige diese an
         */
        searchListView.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
            if (newValue != null) {
                String lang = MainApplication.getInstance().getLanguage();
                searchTextField.setText(newValue.getDisplayName(lang));
                SwingUtilities.invokeLater(() -> graph.showInstanceInView(newValue));
            }
        });

        /*
         * Wenn im Textfeld Enter gedrückt wird, zeige die erste passende Instanz
         */
        searchTextField.setOnKeyPressed(ke -> {

            if (ke.getCode() == KeyCode.ENTER) {
                onSearchInput();

                if (searchListView.getItems().size() > 0) {
                    searchListView.getSelectionModel().selectFirst();
                }
            }
        });

        /**
         * Manuelles setzten des Placeholders für die Listview
         */
        String searchListPlaceholderText = ViewBuilder.getInstance().getText("ui.case_view.search_list_view_placeholder");
        searchListView.setPlaceholder(new Label(searchListPlaceholderText));

    }

    /**
     * Wird aufgerufen, wenn nach einer Instanz im Fallgraphen gesucht wird
     */
    @FXML
    private void onSearchInput() {
        List<CoraInstanceModel> searchResults = graph.findDisplayedInstances(searchTextField.getText());
        searchListView.getItems().setAll(searchResults);
    }

    /**
     * Läd Jena-Rules aus einer Text-Datei und wendet diese auf den aktuellen Fallgraphen an. Das
     * Ergebnis wird in einem neuen Reiter geöffnet.
     * @throws IOException
     */
    @SuppressWarnings("unused")
    @FXML
    private void onApplyRuleset() throws IOException {
        File file = (new FileChooser()).showOpenDialog(stage);

        final String fileName = (file == null)? null : file.getAbsolutePath();
        if(fileName == null) {
            return;
        }

        final WaitViewController waitView = Commons.createWaitScreen(stage);
        waitView.setText("Regeln werden angewendet...");

        final CoraCaseModel oldcase = model.getFactory().getCase();
        Task<CoraCaseModel> loadCaseTask = new Task<CoraCaseModel>() {
            @Override
            protected CoraCaseModel call() throws Exception {
                JenaRulesEngine jre = new JenaRulesEngine();
                return jre.applyRuleset(oldcase, fileName);
            }
        };

        loadCaseTask.stateProperty().addListener((ov, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                waitView.close();
                try {
                    MainApplication.getInstance().getMainAppView().showCase(loadCaseTask.getValue(), "(Generated)");
                } catch (Exception e) {
                    Commons.showException(e);
                }
            } else if(newState == Worker.State.FAILED) {
                waitView.close();
                Commons.showException(loadCaseTask.getException());
            }
        });

        new Thread(loadCaseTask).start();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Entfernt ein ObjectProperty und versteckt die darauf folgenden Instanzen und ObjectProperties
     * @param subject Das Subjekt des zu löschenden Properties
     * @param predicat Das zu löschende ObjectProperty
     * @param object Das Objekt des zu löschenden ObjectProperties
     */
    public void removeObjectRelation(CoraInstanceModel subject,
                                     CoraObjectPropertyModel predicat,
                                     CoraInstanceModel object) {
        subject.removeObjectProperty(predicat, object);
    }

    /**
     * Entfernt ein ObjectProperty und löscht alle darauf folgenden Instanzen und ObjectProperties,
     * sofern diese Teil der Anwendungsontologie sind.
     * @param subject Das Subjekt des zu löschenden Properties
     * @param predicat Das zu löschende ObjectProperty
     * @param object Das Objekt des zu löschenden ObjectProperties
     */
    public void removeObjectRelationRecursive(CoraInstanceModel subject,
                                              CoraObjectPropertyModel predicat,
                                              CoraInstanceModel object) {
        subject.removeObjectPropertiesRecursive(predicat, object);
    }

    /**
     * Setzt die (Fall/Fallbeschreibung-) Instanz, die von diesem CaseViewer angezeigt wird (Ausgangspunkt)
     * @param model Ausgangspunkt für die Ansicht des Fallgraphen
     */
    public void showInstance(CoraInstanceModel model) {
        SwingUtilities.invokeLater(() -> graph.createGraphFromInstance(model));
        if(model.getFactory().getCase() != null) {
            model.getFactory().getCase().addOnChangeHandler(this);
        }

        this.model = model;

        /* Aktiviere den Save-Button, wenn der Fall eine ID hat. */
        if(model.getFactory().getCase().getCaseId() != null) {
            btnSave.setDisable(false);
        }
    }

    private void createAndSetSwingContent(final SwingNode swingNode, final SwingNode navNode) {

        //navNode.resize(400, 400);
        SwingUtilities.invokeLater(() -> {
            //Workaround für den getClipBounds() bug!
            swingNode.resize(800, 600);
            swingNode.setContent(graph);

            navNode.setContent(graph.createNavigationView());
        });
    }

    @SuppressWarnings("unused")
    @FXML
    private void onSaveCase() {
        CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();
        CoraCaseModel caseModel = model.getFactory().getCase();

        if(caseModel == null) {
            System.err.println("Kein FalL!");
            return;
        }

        System.out.println("Saving!");
        caseBase.save(caseModel);
    }

    /**
     * Erzeugt eine neue CBR-Fallbeschreibung aus dem aktuellen Fall.
     * @throws Exception
     */
    @SuppressWarnings("unused")
    @FXML
    private void onCreateCBRRequest() throws Exception {
        CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();
        CoraCaseModel newcase = caseBase.createTemporaryCase();
        CoraCaseModel oldcase = model.getFactory().getCase();

        PartialCaseCopier.copyCaseDescription(oldcase, newcase);

        FXMLLoader loader = ViewBuilder.getInstance().createLoader(QUERY_VIEW_VIEW_FILE);
        AnchorPane pane = loader.load();

        Stage stage = new Stage();
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setTitle(ViewBuilder.getInstance().getText("ui.query_view.title"));
        stage.show();

        QueryViewController controller = loader.getController();
        controller.setStage(stage);
        controller.setCase(newcase);
    }

    /**
     * Wird aufgerufen, wenn der Fallgraph als Bild gespeichert werden soll
     * @throws IOException Wenn der ausgewählte Speicher-Pfad ungültig ist
     */
    @SuppressWarnings("unused")
    @FXML
    private void onExportAsImage() throws IOException {
        FileChooser fileChooser = new FileChooser();
        File outputFile = fileChooser.showSaveDialog(null);
        if(outputFile != null) {
            graph.exportAsImage(false, outputFile);
        }
    }

    /**
     * Wenn eine Instanz ausgewählt wird, zeige deren Daten-Properties in der Tabelle an.
     * @param newSelection Die ausgewählte Instanz
     */
    private void onChangeSelection(CoraInstanceModel newSelection) {
        if(showDataPropertiesTask != null) {
            showDataPropertiesTask.cancel();
            showDataPropertiesTask = null;
        }

        if(newSelection == null) {
            currentSelection.setValue(null);
            return;
        }

        currentSelection.setValue(newSelection);

        showDataPropertiesTask = new Task<ObservableList<DataPropertyAssertion>>() {
            @Override
            protected ObservableList<DataPropertyAssertion> call() throws Exception {
                return FXCollections.observableArrayList(newSelection.getDataPropertyAssertions());
            }
        };

        showDataPropertiesTask.stateProperty().addListener((ov, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                tblDataProperties.setItems(showDataPropertiesTask.getValue());
            }
        });

        new Thread(showDataPropertiesTask).start();
    }

    @Override
    public void onAddInstance(CoraInstanceModel instance) {

    }

    @Override
    public void onCreateObjectRelation(CoraObjectPropertyModel objectProperty,
                                       CoraInstanceModel subject,
                                       CoraInstanceModel object) {
        System.out.println("Erzeuge graph relation");
        SwingUtilities.invokeLater(() -> {
            final String lang = MainApplication.getInstance().getLanguage();
            graph.addInstance(subject, object, objectProperty, objectProperty.getDisplayName(lang));
        });
    }

    @Override
    public void onCreateDataRelation(DataPropertyAssertion assertion) {
        CoraInstanceModel instanceModel = assertion.getSubject();
        if(instanceModel.equals(currentSelection.getValue())) {
            tblDataProperties.getItems().add(assertion);
        }
    }

    @Override
    public void onDeleteDataRelation(DataPropertyAssertion assertion) {
        if(tblDataProperties.getItems().contains(assertion)) {
            tblDataProperties.getItems().remove(assertion);
        }
    }

    @Override
    public void onDeleteObjectRelation(ObjectPropertyAssertion assertion) {
        CoraInstanceModel subject = assertion.getSubject();
        CoraInstanceModel object = assertion.getObject();
        CoraObjectPropertyModel property = assertion.getPredicat();

        SwingUtilities.invokeLater(() -> graph.removeRelation(subject, property, object));
    }

    @SuppressWarnings("unused")
    @FXML
    private void onAddDataProperty() {
        if(currentSelection.getValue() != null) {
            DataPropertyEditorFactory.showEditor(currentSelection.getValue(), null, null, stage);
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void onRemoveDataProperty() {
        if(currentSelection.getValue() != null) {
            DataPropertyAssertion assertion = tblDataProperties.getSelectionModel().getSelectedItem();
            if(assertion == null) {
                return;
            }

            currentSelection.getValue().removePropertyAssertion(assertion);
        }
    }

    /**
     * Wird aufgerufen, wenn der Fall unter einer neuen FallID gespeichert (dupliziert)
     * werden soll
     */
    @SuppressWarnings("unused")
    @FXML
    private void onSaveAsNew() {
        CoraCaseModel caseModel = model.getFactory().getCase();
        SaveAsNewViewController.showSaveAsNew(stage, caseModel);
    }

    /**
     * Wird aufgerufen, wenn der Fall im XML-Format gespeichert werden soll.
     */
    @SuppressWarnings("unused")
    @FXML
    private void onSaveAsXML() {
        CoraCaseModel caseModel = model.getFactory().getCase();
        FileChooser fileChooser = new FileChooser();
        File outputFile = fileChooser.showSaveDialog(null);
        if(outputFile != null) {
            MainApplication.getInstance().getCaseBase().saveAsFile(caseModel, outputFile);
        }
    }

}
