package view.graphview;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.layout.Border;
import mainapp.MainApplication;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.export.SceneExporter;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import view.graphview.menus.EdgeMenu;
import view.graphview.menus.NodeMenu;
import view.graphview.models.EdgeModel;
import view.graphview.models.EdgeWidget;
import view.graphview.models.NodeModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by daniel on 23.08.14.
 *
 * Erzeugt einen neuen Fall-Graph als Swing-Komponente
 */
public class GraphViewComponent extends JPanel {

    private SimpleObjectProperty<CoraInstanceModel> selection = new SimpleObjectProperty<>();
    private SimpleObjectProperty<EventHandler<CreateRelationEvent>> onCreateRelation = new SimpleObjectProperty<>();
    private SimpleObjectProperty<EventHandler<DeleteInstanceEvent>> onDeleteInstance = new SimpleObjectProperty<>();
    private SimpleObjectProperty<EventHandler<DeleteRelationEvent>> onDeleteRelation = new SimpleObjectProperty<>();


    private InstanceGraph scene;
    private Map<CoraInstanceModel, NodeModel> nodes = new HashMap<CoraInstanceModel, NodeModel>();

    public GraphViewComponent() {
        scene = new InstanceGraph();

        //"Übersetze" das InstanceGraph-Widget-Selection-Event in ein CoraInstanceModel-Selection-Event
        scene.currentSelectionProperty().addListener((ov, oldSelection, newSelection) -> {
            if(newSelection != null) {
                setSelection(newSelection.getModel().getModel());
            } else {
                setSelection(null);
            }
        });

        GraphViewComponent self = this;
        scene.getNodeMenu().setActionHandler(new NodeMenu.NodeActionHandler() {
            @Override
            public void onAddNode(InstanceWidget parentWidget) {
                if(onCreateRelation.getValue() != null) {
                    Platform.runLater(() ->
                            onCreateRelation.getValue().handle(new CreateRelationEvent(self, parentWidget.getModel().getModel())));
                }
            }

            @Override
            public void onDeleteNode(InstanceWidget parentWidget) {
                if(onDeleteInstance.getValue() != null) {
                    Platform.runLater(() ->
                            onDeleteInstance.getValue().handle(new DeleteInstanceEvent(self, parentWidget.getModel().getModel())));
                }
            }
        });

        scene.getEdgeMenu().setActionHandler(new EdgeMenu.EdgeActionHandler() {
            @Override
            public void onDeleteEdge(ConnectionWidget edgeWidget) {
                if(onDeleteRelation.getValue() != null) {
                    Platform.runLater(() -> {
                        EdgeWidget w = (EdgeWidget) edgeWidget;
                        EdgeModel model = w.getModel();

                        onDeleteRelation.getValue().handle(new DeleteRelationEvent(self, model.getSource().getModel(), model.getProperty(), model.getTarget().getModel()));
                    });
                }
            }
        });

        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        JComponent sceneView = scene.createView();

        scrollPane.setViewportView(sceneView);
        add(scrollPane, BorderLayout.CENTER);
        //add(scene.createSatelliteView(), BorderLayout.WEST);

        setPreferredSize(new Dimension(800, 600));
        setSize(getPreferredSize());
    }

    public JPanel createNavigationView() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(scene.createSatelliteView(), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(180, 200));
        panel.setSize(panel.getPreferredSize());
        return panel;
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
        instanceModel.setModel(instance);

        visited.put(instance, instanceModel);
        scene.addNode(instanceModel);

        final String lang = MainApplication.getInstance().getLanguage();

        Map<CoraObjectPropertyModel, Set<CoraInstanceModel>> objectProperties = instance.getObjectProperties();
        for(Map.Entry<CoraObjectPropertyModel, Set<CoraInstanceModel>> e : objectProperties.entrySet()) {
            Set<CoraInstanceModel> set = e.getValue();
            for(CoraInstanceModel i : set) {
                NodeModel source = instanceModel;
                NodeModel target = addInstanceRec(i, visited);

//                EdgeModel edge = new EdgeModel(e.getKey().toString());
                EdgeModel edge = new EdgeModel(e.getKey(), e.getKey().getDisplayName(lang));
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
    public void addInstance(CoraInstanceModel parent, CoraInstanceModel child, CoraObjectPropertyModel property, String relation) {
        NodeModel source = addInstanceRec(parent, nodes);
        NodeModel target = addInstanceRec(child, nodes);

        EdgeModel edge = new EdgeModel(property, relation);
        edge.setSource(source);
        edge.setTarget(target);

        scene.addConnection(edge);

        scene.forceLayout();
        scene.validate();
    }

    public void removeRelation(CoraInstanceModel subject, CoraObjectPropertyModel property, CoraInstanceModel object) {
        Collection<EdgeModel> models = scene.getEdges();
        for(EdgeModel m : models) {
            if(m.getSource().getModel().equals(subject) &&
               m.getTarget().getModel().equals(object) &&
               m.getProperty().equals(property)) {
                System.out.println("Found edge model.");
                removeEdgeRec(m);
            }
        }

        System.out.println("Edge model removing...");
    }

    /**
     * Entfernt eine Kante und alle daran hängenden Instanzen aus der
     * Graph-Ansicht.
     * @param model Die zu entfernende Kante
     */
    private void removeEdgeRec(EdgeModel model) {
        NodeModel target = model.getTarget();
        // Wenn diese Kante die einzige ist, die auf diesen
        // Knoten verweist, entferne den Knoten aus der Ansicht
        if(countIncommingConnections(target) == 1) {
            for(EdgeModel e : listNodeConnections(target)) {
                removeEdgeRec(e);
            }

            scene.removeNode(target);
            nodes.remove(target.getModel());
        }

        scene.removeEdge(model);
    }

    /**
     * Gibt alle Kanten zurück, die von node ausgehen.
     * @param node
     * @return
     */
    private List<EdgeModel> listNodeConnections(NodeModel node) {
        ArrayList<EdgeModel> edges = new ArrayList<>();
        for(EdgeModel e : scene.getEdges()) {
            if(e.getSource().getModel().equals(node.getModel())) {
                edges.add(e);
            }
        }

        return edges;
    }

    /**
     * Gibt die Anzahl der Kanten zurück, die diesen Knoten als Ziel
     * haben.
     * @param node
     * @return
     */
    private int countIncommingConnections(NodeModel node) {
        int counter = 0;
        for(EdgeModel e : scene.getEdges()) {
            if(e.getTarget().getModel().equals(node.getModel())) {
                counter++;
            }
        }

        return counter;
    }

    /**
     * Getter für die aktuell ausgewählte Instanz
     * @return Der aktuelle Event-Handler
     */
    @SuppressWarnings("unused")
    public CoraInstanceModel getSelection() {
        return selection.get();
    }

    /**
     * Property, das die aktuell ausgewählte Instanz enthält
     * @return Property, das die aktuell ausgewählte Instanz enthält
     */
    @SuppressWarnings("unused")
    public SimpleObjectProperty<CoraInstanceModel> selectionProperty() {
        return selection;
    }

    /**
     * Setter für die aktuell ausgewählte Instanz
     * TODO: Änderungen durch diesen Setter werden in der Graph-Ansicht nicht reflektiert
     * @param selection Die auszuwählende Instanz
     */
    @SuppressWarnings("unused")
    public void setSelection(CoraInstanceModel selection) {
        this.selection.set(selection);
    }

    /**
     * Event-Handler getter
     * @return Der aktuelle Event-Handler
     */
    @SuppressWarnings("unused")
    public EventHandler<CreateRelationEvent> getOnCreateRelation() {
        return onCreateRelation.get();
    }

    /**
     * Getter für das Event-Handler Property
     * @return Das Event-Handler Property
     */
    @SuppressWarnings("unused")
    public SimpleObjectProperty<EventHandler<CreateRelationEvent>> onCreateRelationProperty() {
        return onCreateRelation;
    }

    /**
     * Event-Handler setter
     * @param onCreateRelation Der neue Event-Handler
     */
    public void setOnCreateRelation(EventHandler<CreateRelationEvent> onCreateRelation) {
        this.onCreateRelation.set(onCreateRelation);
    }

    /**
     * Event-Handler getter
     * @return Der aktuelle Event-Handler
     */
    @SuppressWarnings("unused")
    public EventHandler<DeleteInstanceEvent> getOnDeleteInstance() {
        return onDeleteInstance.get();
    }

    /**
     * Getter für das Event-Handler Property
     * @return Das Event-Handler Property
     */
    @SuppressWarnings("unused")
    public SimpleObjectProperty<EventHandler<DeleteInstanceEvent>> onDeleteInstanceProperty() {
        return onDeleteInstance;
    }

    /**
     * Event-Handler setter
     * @param onDeleteInstance Der neue Event-Handler
     */
    @SuppressWarnings("unused")
    public void setOnDeleteInstance(EventHandler<DeleteInstanceEvent> onDeleteInstance) {
        this.onDeleteInstance.set(onDeleteInstance);
    }

    public EventHandler<DeleteRelationEvent> getOnDeleteRelation() {
        return onDeleteRelation.get();
    }

    public SimpleObjectProperty<EventHandler<DeleteRelationEvent>> onDeleteRelationProperty() {
        return onDeleteRelation;
    }

    public void setOnDeleteRelation(EventHandler<DeleteRelationEvent> onDeleteRelation) {
        this.onDeleteRelation.set(onDeleteRelation);
    }

    /**
     * Event-Klasse, die verwendet wird, wenn der Nutzer eine neue Relation erstellen möchte.
     */
    public class CreateRelationEvent extends ActionEvent {
        private CoraInstanceModel parentInstance;

        /**
         *
         * @param graphView Die Quelle des Events - üblicherweise die <code>GraphViewComponent</code>
         * @param parentInstance Das Subjekt der zu erstellenden Relation
         */
        public CreateRelationEvent(GraphViewComponent graphView, CoraInstanceModel parentInstance) {
            super(graphView, null);
            this.parentInstance = parentInstance;
        }

        /**
         * Das Subjekt der zu erstellenden Relation
         * @return Das Subjekt
         */
        @SuppressWarnings("unused")
        public CoraInstanceModel getParentInstance() {
            return parentInstance;
        }

        /**
         * Das Subjekt der zu erstellenden Relation
         * @param parentInstance Das Subjekt
         */
        @SuppressWarnings("unused")
        public void setParentInstance(CoraInstanceModel parentInstance) {
            this.parentInstance = parentInstance;
        }
    }

    /**
     * Event-Klasse, die verwendet wird, wenn der Nutzer eine Instanz löschen will
     */
    public class DeleteInstanceEvent extends ActionEvent {
        private CoraInstanceModel parentInstance;

        /**
         *
         * @param graphView Die Quelle des Events - üblicherweise die <code>GraphViewComponent</code>
         * @param parentInstance Die zu löschende Instanz
         */
        public DeleteInstanceEvent(GraphViewComponent graphView, CoraInstanceModel parentInstance) {
            super(graphView, null);
            this.parentInstance = parentInstance;
        }

        /**
         * Die zu löschende Instanz
         * @return Die zu löschende Instanz
         */
        @SuppressWarnings("unused")
        public CoraInstanceModel getParentInstance() {
            return parentInstance;
        }

        /**
         * Die zu löschende Instanz
         * @param parentInstance Die zu löschende Instanz
         */
        @SuppressWarnings("unused")
        public void setParentInstance(CoraInstanceModel parentInstance) {
            this.parentInstance = parentInstance;
        }
    }

    public class DeleteRelationEvent extends ActionEvent {

        private CoraInstanceModel subject;
        private CoraInstanceModel object;
        private CoraObjectPropertyModel predicat;

        public DeleteRelationEvent(GraphViewComponent graphView, CoraInstanceModel subject,
                                   CoraObjectPropertyModel predicat,
                                   CoraInstanceModel object) {
            super(graphView, null);
            this.subject = subject;
            this.object = object;
            this.predicat = predicat;
        }

        public CoraInstanceModel getSubject() {
            return subject;
        }

        public void setSubject(CoraInstanceModel subject) {
            this.subject = subject;
        }

        public CoraInstanceModel getObject() {
            return object;
        }

        public void setObject(CoraInstanceModel object) {
            this.object = object;
        }

        public CoraObjectPropertyModel getPredicat() {
            return predicat;
        }

        public void setPredicat(CoraObjectPropertyModel predicat) {
            this.predicat = predicat;
        }
    }
}

