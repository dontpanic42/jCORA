package view.graphview;

import javafx.application.Platform;
import javafx.scene.layout.Border;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;
import view.graphview.menus.NodeMenu;
import view.graphview.models.EdgeModel;
import view.graphview.models.NodeModel;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * Created by daniel on 23.08.14.
 */
public class GraphViewComponent extends JPanel {

    public interface GraphViewActionHandler {
        public void onAddRelation(GraphViewComponent graph, CoraInstanceModel parent);

        public void onDeleteInstance(GraphViewComponent graph, CoraInstanceModel model);
    }

    private InstanceGraph scene;
    private Map<CoraInstanceModel, NodeModel> nodes = new HashMap<CoraInstanceModel, NodeModel>();

    private GraphViewActionHandler actionHandler = null;

    public GraphViewComponent() {
        scene = new InstanceGraph();
        scene.getNodeMenu().setActionHandler(new NodeMenu.NodeActionHandler() {
            @Override
            public void onAddNode(InstanceWidget parentWidget) {
                onAddNodeHandler(parentWidget);
            }

            @Override
            public void onDeleteNode(InstanceWidget parentWidget) {
                onDeleteNodeHandler(parentWidget);
            }
        });

        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        JComponent sceneView = scene.createView();

        scrollPane.setViewportView(sceneView);
        add(scrollPane, BorderLayout.CENTER);
        add(scene.createSatelliteView(), BorderLayout.WEST);

        setPreferredSize(new Dimension(800, 600));
        setSize(getPreferredSize());
    }

    public void forceRedraw() {
        scene.validate();
        scene.revalidate();
        scene.revalidate(true);
        scene.validate();
    }

    public void forceLayout() {
        scene.forceLayout();
    }

    public void setActionHandler(GraphViewActionHandler handler) {
        this.actionHandler = handler;
    }

    private void onAddNodeHandler(InstanceWidget parentWidget) {
        if(actionHandler == null) {
            return;
        }

        GraphViewComponent self = this;

        //Run the event handler on the javafx-Thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                actionHandler.onAddRelation(self, parentWidget.getNodeModel().getModel());
            }
        });
    }

    private void onDeleteNodeHandler(InstanceWidget parentWidget) {
        if(actionHandler == null) {
            return;
        }

        GraphViewComponent self = this;

        //Run the event handler on the javafx-Thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                actionHandler.onDeleteInstance(self, parentWidget.getNodeModel().getModel());
            }
        });
    }

    public void createGraphFromInstance(CoraInstanceModel instance) {
        createGraphFromInstanceRec(instance, nodes);
        scene.forceLayout();
        scene.validate();
    }

    private NodeModel createGraphFromInstanceRec(CoraInstanceModel instance, Map<CoraInstanceModel, NodeModel> visited) {
        if(visited.containsKey(instance)) {
            return visited.get(instance);
        }

        NodeModel instanceModel = new NodeModel();
        instanceModel.setInstanceName(instance.toString());
        instanceModel.setInstanceType(instance.toString());
        instanceModel.setModel(instance);

        visited.put(instance, instanceModel);
        scene.addNode(instanceModel);

        Map<CoraObjectPropertyModel, Set<CoraInstanceModel>> objectProperties = instance.getObjectProperties();
        for(Map.Entry<CoraObjectPropertyModel, Set<CoraInstanceModel>> e : objectProperties.entrySet()) {
            Set<CoraInstanceModel> set = e.getValue();
            for(CoraInstanceModel i : set) {
                NodeModel source = instanceModel;
                NodeModel target = createGraphFromInstanceRec(i, visited);

                EdgeModel edge = new EdgeModel(e.getKey().toString());
                edge.setSource(source);
                edge.setTarget(target);

                scene.addConnection(edge);
            }
        }

        return instanceModel;
    }

    public void addInstance(CoraInstanceModel parent, CoraInstanceModel child, String relation) {
        NodeModel source;
        if(nodes.containsKey(parent)) {
            source = nodes.get(parent);
        } else {
            source = new NodeModel();
            source.setInstanceName(parent.toString());
            source.setInstanceType(parent.toString());
            source.setModel(parent);

            scene.addNode(source);
        }

        NodeModel target;
        if(nodes.containsKey(child)) {
            target = nodes.get(child);
        } else {
            target = new NodeModel();
            target.setInstanceName(child.toString());
            target.setInstanceType(child.toString());
            target.setModel(child);

            scene.addNode(target);
        }

        EdgeModel edge = new EdgeModel(relation);
        edge.setSource(source);
        edge.setTarget(target);

        scene.addConnection(edge);

        scene.forceLayout();
        scene.validate();
    }
}
