package controllers;

import controllers.dataproperty.DataPropertyEditorFactory;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import view.graphview.GraphViewComponent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by daniel on 24.08.14.
 */
public class CaseViewController implements CoraCaseModel.CaseChangeHandler {
    @FXML
    private SwingNode swingNode;

    @FXML
    private TableColumn<DataPropertyAssertion, CoraDataPropertyModel> columnPropertyName;

    @FXML
    private TableColumn<DataPropertyAssertion, String>  columnPropertyValue;

    @FXML
    private TableColumn<DataPropertyAssertion, String> columnPropertyUnit;

    @FXML
    private TableView<DataPropertyAssertion> tblDataProperties;

    private final GraphViewComponent graph = new GraphViewComponent();

    private CoraInstanceModel model;
    private CoraInstanceModel currentSelection;
    private Task<ObservableList<DataPropertyAssertion>> showDataPropertiesTask;

    private Stage stage;

    @FXML
    public void initialize() {
        createAndSetSwingContent(swingNode);

        //Setup Evenent-Handlers
        graph.selectionProperty().addListener((ov, oldSelection, newSelection) ->
                onChangeSelection(oldSelection, newSelection));
        graph.setOnCreateRelation((ev) -> AddObjectPropertyViewController.showAddRelation(stage, ev.getParentInstance()));

        tblDataProperties.setPlaceholder(new Label("Keine Daten vorhanden"));

        columnPropertyName.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<DataPropertyAssertion, CoraDataPropertyModel>,
                        ObservableValue<CoraDataPropertyModel>>() {
            @Override
            public ObservableValue<CoraDataPropertyModel> call(TableColumn.CellDataFeatures<DataPropertyAssertion, CoraDataPropertyModel> p) {
                return new ReadOnlyObjectWrapper<>(p.getValue().getPredicat());
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
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void showInstance(CoraInstanceModel model) {
        graph.createGraphFromInstance(model);
        if(model.getFactory().getCase() != null) {
            model.getFactory().getCase().addOnChangeHandler(this);
        }

        this.model = model;
    }

    private void createAndSetSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Workaround für den getClipBounds() bug!
                swingNode.resize(800, 600);
                swingNode.setContent(graph);
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                graph.addInstance(subject, object, objectProperty.toString());
            }
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
