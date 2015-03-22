package controllers;

import com.sun.glass.ui.Application;
import controllers.commons.ThrowableErrorViewController;
import controllers.commons.WaitViewController;
import controllers.dataproperty.DataPropertyEditorFactory;
import controllers.queryeditor.QueryViewController;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.Language;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import models.datatypes.TypedValue;
import models.ontology.CoraDataPropertyModel;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;
import models.ontology.assertions.DataPropertyAssertion;
import models.ontology.assertions.ObjectPropertyAssertion;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import services.adaption.utility.PartialCaseCopier;
import services.rules.jenarules.JenaRulesEngine;
import view.Commons;
import view.viewbuilder.ViewBuilder;
import view.graphview.GraphViewComponent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by daniel on 24.08.14.
 */
public class CaseViewController implements CoraCaseModel.CaseChangeHandler {

    private static final String QUERY_VIEW_VIEW_FILE = "views/queryeditor/queryView.fxml";

    @FXML
    private SwingNode swingNode;

    @FXML
    private SwingNode navNode;

    @FXML
    private TableColumn<DataPropertyAssertion, CoraDataPropertyModel> columnPropertyName;

    @FXML
    private TableColumn<DataPropertyAssertion, String>  columnPropertyValue;

    @FXML
    private TableColumn<DataPropertyAssertion, String> columnPropertyUnit;

    @FXML
    private TableView<DataPropertyAssertion> tblDataProperties;

    @FXML
    private Button btnSave;

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<CoraInstanceModel> searchListView;

    private final GraphViewComponent graph = new GraphViewComponent();

    private CoraInstanceModel model;
    private CoraInstanceModel currentSelection;
    private Task<ObservableList<DataPropertyAssertion>> showDataPropertiesTask;

    private Stage stage;

    @FXML
    public void initialize() {
        createAndSetSwingContent(swingNode, navNode);

        //Setup Evenent-Handlers
        graph.selectionProperty().addListener((ov, oldSelection, newSelection) ->
                onChangeSelection(oldSelection, newSelection));
        graph.setOnCreateRelation((ev) -> AddObjectPropertyViewController.showAddRelation(stage, ev.getParentInstance()));

        graph.setOnDeleteRelation((GraphViewComponent.DeleteRelationEvent ev) -> {
            // Das Event wird im Swing-Thread ausgelöst...
            Application.invokeLater(() -> {
                removeObjectRelation(ev.getSubject(), ev.getPredicat(), ev.getObject());
            });
        });

        tblDataProperties.setPlaceholder(new Label(ViewBuilder.getInstance().getText("ui.case_view.label_no_data_properties")));

        columnPropertyName.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<DataPropertyAssertion, CoraDataPropertyModel>,
                        ObservableValue<CoraDataPropertyModel>>() {
                    @Override
                    public ObservableValue<CoraDataPropertyModel> call(TableColumn.CellDataFeatures<DataPropertyAssertion, CoraDataPropertyModel> p) {
                        return new ReadOnlyObjectWrapper<>(p.getValue().getPredicat());
                    }
                });

        columnPropertyName.setCellFactory(new Callback<TableColumn<DataPropertyAssertion, CoraDataPropertyModel>, TableCell<DataPropertyAssertion, CoraDataPropertyModel>>() {
            @Override
            public TableCell<DataPropertyAssertion, CoraDataPropertyModel> call(TableColumn<DataPropertyAssertion, CoraDataPropertyModel> dataPropertyAssertionCoraDataPropertyModelTableColumn) {
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
                new Callback<TableColumn.CellDataFeatures<DataPropertyAssertion,
                        String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DataPropertyAssertion, String> p) {
                TypedValue object = p.getValue().getObject();
                if(object == null) {
                    return new ReadOnlyObjectWrapper<>("[null]");
                } else {
                    return new ReadOnlyObjectWrapper<>(object.getAsString());
                }
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

        btnSave.setDisable(true);

        setupSearch();
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
                graph.showInstanceInView(newValue);
            }
        });

        /*
         * Wenn im Textfeld Enter gedrückt wird, zeige die erste passende Instanz
         */
        searchTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {

                if (ke.getCode() == KeyCode.ENTER) {
                    onSearchInput();

                    if (searchListView.getItems().size() > 0) {
                        searchListView.getSelectionModel().selectFirst();
                    }
                }
            }
        });

        /**
         * Manuelles setzten des Placeholders für die Listview
         */
        String searchListPlaceholderText = ViewBuilder.getInstance().getText("ui.case_view.search_list_view_placeholder");
        searchListView.setPlaceholder(new Label(searchListPlaceholderText));

    }

    @FXML
    private void onSearchInput() {
        ArrayList<CoraInstanceModel> searchResults = graph.findDisplayedInstances(searchTextField.getText());
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

    public void removeObjectRelation(CoraInstanceModel subject, CoraObjectPropertyModel predicat, CoraInstanceModel object) {
        subject.removeObjectProperty(predicat, object);
    }

    public void showInstance(CoraInstanceModel model) {
        graph.createGraphFromInstance(model);
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Workaround für den getClipBounds() bug!
                swingNode.resize(800, 600);
                swingNode.setContent(graph);


                navNode.setContent(graph.createNavigationView());
            }
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
     * @param oldSelection
     * @param newSelection
     */
    private void onChangeSelection(CoraInstanceModel oldSelection, CoraInstanceModel newSelection) {
        if(showDataPropertiesTask != null) {
            showDataPropertiesTask.cancel();
            showDataPropertiesTask = null;
        }

        if(newSelection == null) {
            currentSelection = null;
            return;
        }

        currentSelection = newSelection;

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
        if(instanceModel.equals(currentSelection)) {
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

        Platform.runLater(() -> {
            graph.removeRelation(subject, property, object);
        });
    }

    @SuppressWarnings("unused")
    @FXML
    private void onAddDataProperty() {
        if(currentSelection != null) {
            DataPropertyEditorFactory.showEditor(currentSelection , null, null, stage);
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void onRemoveDataProperty() {
        if(currentSelection != null) {
            DataPropertyAssertion assertion = tblDataProperties.getSelectionModel().getSelectedItem();
            if(assertion == null) {
                return;
            }

            currentSelection.removePropertyAssertion(assertion);
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void onSaveAsNew() {
        CoraCaseModel caseModel = model.getFactory().getCase();
        SaveAsNewViewController.showSaveAsNew(stage, caseModel);
    }

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
