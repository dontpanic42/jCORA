package controllers;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;
import view.graphview.GraphViewComponent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by daniel on 24.08.14.
 */
public class CaseViewController implements GraphViewComponent.GraphViewActionHandler,
        CoraCaseModel.CaseChangeHandler {
    @FXML
    private SwingNode swingNode;

    private final GraphViewComponent graph = new GraphViewComponent();

    private CoraInstanceModel model;

    private Stage stage;

    @FXML
    public void initialize() {
        createAndSetSwingContent(swingNode);
        graph.setActionHandler(this);
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
}
