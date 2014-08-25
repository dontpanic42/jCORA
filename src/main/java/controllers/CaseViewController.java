package controllers;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import mainapp.MainApplication;
import models.ontology.CoraInstanceModel;
import view.graphview.GraphViewComponent;

import javax.swing.*;

/**
 * Created by daniel on 24.08.14.
 */
public class CaseViewController implements GraphViewComponent.GraphViewActionHandler {
    @FXML
    private SwingNode swingNode;

    private final GraphViewComponent graph = new GraphViewComponent();

    @FXML
    public void initialize() {
        createAndSetSwingContent(swingNode);
        graph.setActionHandler(this);
    }

    public void showInstance(CoraInstanceModel model) {
        graph.createGraphFromInstance(model);

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

    @Override
    public void onAddRelation(GraphViewComponent graph, CoraInstanceModel parent) {
        AddObjectPropertyViewController.showAddRelation(MainApplication.getInstance().getMainStage(), parent);
    }

    @Override
    public void onDeleteInstance(GraphViewComponent graph, CoraInstanceModel model) {

    }
}
