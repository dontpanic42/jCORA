package controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import models.util.Pair;
import view.graphview.GraphViewComponent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by daniel on 24.08.14.
 */
public class CaseViewController implements GraphViewComponent.GraphViewActionHandler,
        CoraCaseModel.CaseChangeHandler {
    @FXML
    private SwingNode swingNode;

    @FXML
    private TableColumn<Pair<CoraDataPropertyModel, TypedValue>, CoraDataPropertyModel> columnPropertyName;

    @FXML
    private TableColumn<Pair<CoraDataPropertyModel, TypedValue>, String>  columnPropertyValue;

    @FXML
    private TableView<Pair<CoraDataPropertyModel, TypedValue>> tblDataProperties;

    private final GraphViewComponent graph = new GraphViewComponent();

    private CoraInstanceModel model;

    private Stage stage;

    @FXML
    public void initialize() {
        createAndSetSwingContent(swingNode);
        graph.setActionHandler(this);

        tblDataProperties.setPlaceholder(new Label("Keine Daten vorhanden"));

        columnPropertyName.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Pair<CoraDataPropertyModel, TypedValue>, CoraDataPropertyModel>,
                        ObservableValue<CoraDataPropertyModel>>() {
            @Override
            public ObservableValue<CoraDataPropertyModel> call(TableColumn.CellDataFeatures<Pair<CoraDataPropertyModel,
                    TypedValue>, CoraDataPropertyModel> p) {
                return new ReadOnlyObjectWrapper<>(p.getValue().getFirst());
            }
        });

        columnPropertyValue.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Pair<CoraDataPropertyModel, TypedValue>,
                        String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<CoraDataPropertyModel,
                    TypedValue>, String> p) {
                return new ReadOnlyObjectWrapper<>(p.getValue().getSecond().getAsString());
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
     * @param graph
     * @param oldSelection
     * @param newSelection
     */
    @Override
    public void onChangeSelection(GraphViewComponent graph, CoraInstanceModel oldSelection, CoraInstanceModel newSelection) {
        if(newSelection == null) {
            return;
        }

        List<Pair<CoraDataPropertyModel, TypedValue>> props = newSelection.getDataProperties();
        tblDataProperties.setItems(FXCollections.observableArrayList(props));
    }

    @Override
    public void onAddRelation(GraphViewComponent graph, CoraInstanceModel parent) {
        AddObjectPropertyViewController.showAddRelation(stage, parent);
    }

    @Override
    public void onDeleteInstance(GraphViewComponent graph, CoraInstanceModel model) {

    }

    @Override
    public void onAddInstance(CoraInstanceModel instance) {

    }

    @Override
    public void onCreateObjectRelation(CoraObjectPropertyModel objectProperty, CoraInstanceModel subject, CoraInstanceModel object) {
        System.out.println("Erzeuge graph relation");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                graph.addInstance(subject, object, objectProperty.toString());
            }
        });
    }

    @FXML
    private void onAddDataProperty() {

    }

    @FXML
    private void onRemoveDataProperty() {

    }

    @FXML
    private void onSaveAsNew() {
        CoraCaseModel caseModel = model.getFactory().getCase();
        SaveAsNewViewController.showSaveAsNew(stage, caseModel);
    }

}
