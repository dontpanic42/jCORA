package view.graphview;

import javafx.application.Platform;
import javafx.scene.layout.Border;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;
import org.netbeans.api.visual.export.SceneExporter;
import view.graphview.menus.NodeMenu;
import view.graphview.models.EdgeModel;
import view.graphview.models.NodeModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * Created by daniel on 23.08.14.
 *
 * Erzeugt einen neuen Fall-Graph als Swing-Komponente
 */
public class GraphViewComponent extends JPanel {

    /**
     * Eventhandler Interface
     */
    public interface GraphViewActionHandler {
        /**
         * Wird aufgerufen, wenn der nutzer eine neue Relation anlegen will.
         * @param graph Der betreffende Graph
         * @param parent Die Instanz, auf der die Relation angelegt werden soll
         */
        public void onAddRelation(GraphViewComponent graph, CoraInstanceModel parent);

        /**
         * Wird aufgerufen, wenn der Nutzer eine Instanz löschen will.
         * @param graph Der betreffende Graph
         * @param model Die Instanz, die gelöscht werden soll
         */
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

    /**
     * Erzwingt ein erneutes Rendern des Graphen.
     */
    public void forceRedraw() {
        scene.validate();
        scene.revalidate();
        scene.revalidate(true);
        scene.validate();
    }

    /**
     * Erzwing eine neuausrichtung des Graphen (Baum-Layout)
     */
    public void forceLayout() {
        scene.forceLayout();
    }

    /**
     * Setzt den Actionhandler
     * @param handler
     */
    public void setActionHandler(GraphViewActionHandler handler) {
        this.actionHandler = handler;
    }

    /**
     * Wird aufgerufen, wenn der Nutzer auf "Relation hinzufügen..." klickt
     * @param parentWidget Das Widget, auf das geklickt wurde
     */
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

    /**
     * Wird aufgerufen, wenn der Nutzer auf "Instanz löschen" klickt.
     * @param parentWidget Das Widget, auf das geklickt wurde
     */
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

    /**
     * Erzeugt einen (neuen) Graphen ausgehend von einer Instanz (rekursiv)
     * @param instance Die Ausgangsinstanz
     */
    public void createGraphFromInstance(CoraInstanceModel instance) {
        addInstanceRec(instance, nodes);
        scene.forceLayout();
        scene.validate();
    }

    /**
     * Fügt dem Graphen eine Instanz rekursiv mit allen Relationen und dafür notwendigen Instanzen hinzu.
     * @param instance Die Ausgangsinstanz
     * @param visited Liste mit im Graph vorhandenen Instanzen
     * @return Das hinzugefügte Graph-Knotenmodel
     */
    private NodeModel addInstanceRec(CoraInstanceModel instance, Map<CoraInstanceModel, NodeModel> visited) {
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
                NodeModel target = addInstanceRec(i, visited);

                EdgeModel edge = new EdgeModel(e.getKey().toString());
                edge.setSource(source);
                edge.setTarget(target);

                scene.addConnection(edge);
            }
        }

        return instanceModel;
    }

    /**
     * Exportiert den Graphen als Bild-Datei
     * @param viewportOnly Exportiere nur den sichtbaren Graphen
     * @param target Die Zieldatei
     * @throws IOException
     */
    public void exportAsImage(boolean viewportOnly, File target) throws IOException {
        int width = scene.getBounds().width;
        int height = scene.getBounds().height;
        SceneExporter.createImage(scene, target, SceneExporter.ImageType.PNG, SceneExporter.ZoomType.CURRENT_ZOOM_LEVEL,
                viewportOnly, false, 0, width, height);
    }

    /**
     * Fügt einem (vorhandenen) Graphen eine neue Relation und ggf. Instanz hinzu.
     * @param parent Die vorhandene Eltern-Instanz (Subjekt)
     * @param child Die Relation (Prädikat)
     * @param relation Die Kind-Instanz (Objekt)
     */
    public void addInstance(CoraInstanceModel parent, CoraInstanceModel child, String relation) {
        NodeModel source = addInstanceRec(parent, nodes);
        NodeModel target = addInstanceRec(child, nodes);

        EdgeModel edge = new EdgeModel(relation);
        edge.setSource(source);
        edge.setTarget(target);

        scene.addConnection(edge);

        scene.forceLayout();
        scene.validate();
    }
}
