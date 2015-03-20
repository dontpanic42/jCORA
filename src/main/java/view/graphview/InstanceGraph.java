package view.graphview;


//import com.sun.javafx.accessible.providers.SelectionProvider;
import javafx.beans.property.SimpleObjectProperty;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.graph.layout.TreeGraphLayout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.action.SelectAction;
import view.graphview.menus.EdgeMenu;
import view.graphview.menus.NodeMenu;
import view.graphview.models.EdgeModel;
import view.graphview.models.EdgeWidget;
import view.graphview.models.NodeModel;

import java.awt.*;
import java.util.Collection;

/**
 * Created by daniel on 23.08.14.
 */
public class InstanceGraph extends GraphScene<NodeModel, EdgeModel> {

    /**
     * Die Hauptschicht, auf der die Instanzen angezeigt werden
     */
    private LayerWidget mainLayer;

    /**
     * Die Schicht, auf der die Verbindungen angezeigt werden
     */
    private LayerWidget connectionLayer;

    /**
     * Der Baum-Layoutmanager
     */
    private SceneLayout sceneGraphLayout;

    /**
     * Das Instanz-Rechtsklick-Popupmenü
     */
    private NodeMenu nodeMenu;
    private EdgeMenu edgeMenu;

    private SimpleObjectProperty<InstanceWidget> currentSelection = new SimpleObjectProperty<>();

    /**
     * Konstruktor, erzeugt eine neue, leere VL-Graph instanz.
     */
    public InstanceGraph() {
        super();

        nodeMenu = new NodeMenu(this);
        edgeMenu = new EdgeMenu(this);

        connectionLayer = new LayerWidget(this);

        mainLayer = new LayerWidget(this);

        //Die Reihenfolge ist wichtig: Erst mainLayer, _dann_ connectionLayer. Sonst kommt es zu dem
        //'not a Node' fehler.
        addChild(mainLayer);
        addChild(connectionLayer);

        TreeGraphLayout<NodeModel, EdgeModel> graphLayout = new TreeGraphLayout<>(this, 0, 0, 50, 150, false);
        sceneGraphLayout = LayoutFactory.createSceneGraphLayout(this, graphLayout);

        getActions().addAction(ActionFactory.createPanAction());
        getActions().addAction(ActionFactory.createZoomAction());
    }

    /**
     * Erzwingt die Neuausrichtung des Graphen (Baum-Layout)
     */
    public void forceLayout() {
        sceneGraphLayout.invokeLayout();
    }

    /**
     * Fügt dem Graphen eine neue Relation hinzu
     * @param edge Das Relationsmodel
     */
    public void addConnection(EdgeModel edge) {
        addEdge(edge);

        setEdgeSource(edge, edge.getSource());
        setEdgeTarget(edge, edge.getTarget());
    }

    public void removeConnection(EdgeModel model) {
        Collection<EdgeModel> edges = getEdges();
        for(EdgeModel edge : edges) {

        }
    }

    /**
     * Gibt das Popupmenü zurück, das angezeigt wird, wenn der Nutzer mit der rechten Maustaste auf eine
     * Instanz klickt.
     * @return Das Popupmenü
     */
    public NodeMenu getNodeMenu() {
        return nodeMenu;
    }

    public EdgeMenu getEdgeMenu() {
        return edgeMenu;
    }

    /**
     * Wird in Folge einer <code>addNode</code> operation aufgerufen. Erzeugt und konfiguriert ein neues Widget für ein
     * <code>NodeModel</code>.
     * @param nodeModel Das hinzuzufügende Knoten-Model
     * @return Ein neues Widget
     */
    @Override
    protected Widget attachNodeWidget(NodeModel nodeModel) {
        InstanceWidget widget = new InstanceWidget(this);
        widget.getActions().addAction(ActionFactory.createMoveAction());
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(nodeMenu));
        widget.setModel(nodeModel);

        mainLayer.addChild(widget);
        return widget;
    }

    /**
     * Wird in Folge einer <code>addConnection</code> operation aufgerufen. Erzeugt und konfiguriert ein neues
     * Widget für ein <code>EdgeModel</code>.
     * @param edgeModel Das Kanten-Model
     * @return ein neues Kanten-Widget
     */
    @Override
    protected Widget attachEdgeWidget(EdgeModel edgeModel) {
        EdgeWidget connectionWidget = new EdgeWidget(edgeModel, this);

        connectionWidget.getActions().addAction(ActionFactory.createPopupMenuAction(edgeMenu));
        connectionWidget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);

        LabelWidget edgeLabel = new LabelWidget(this);
        edgeLabel.setOpaque(true);
        edgeLabel.setLabel(edgeModel.getLabel());

        connectionWidget.addChild(edgeLabel);
        connectionWidget.setConstraint(edgeLabel, LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER, 0.5f);

        connectionLayer.addChild(connectionWidget);

        return connectionWidget;
    }

    /**
     * Setzt die Quelle einer Kante
     * @param s Das Kantenmodell
     * @param oldSourceNode Die vorherige Quelle
     * @param sourceNode Die aktuelle (neue) Quelle
     */
    @Override
    protected void attachEdgeSourceAnchor(EdgeModel s, NodeModel oldSourceNode, NodeModel sourceNode) {
        ConnectionWidget c = (ConnectionWidget) findWidget(s);
        Widget source = (Widget) findWidget(sourceNode);
        c.setSourceAnchor(AnchorFactory.createRectangularAnchor(source));
    }

    /**
     * Setzt das Ziel einer Kante
     * @param s Das Kantenmodell
     * @param oldTargetNode Das vorherige Ziel
     * @param targetNode Das aktuelle (neue) Ziel
     */
    @Override
    protected void attachEdgeTargetAnchor(EdgeModel s, NodeModel oldTargetNode, NodeModel targetNode) {
        ConnectionWidget c = (ConnectionWidget) findWidget(s);
        Widget target = findWidget(targetNode);
        c.setTargetAnchor(AnchorFactory.createRectangularAnchor(target));
    }


    public InstanceWidget getCurrentSelection() {
        return currentSelection.get();
    }

    public SimpleObjectProperty<InstanceWidget> currentSelectionProperty() {
        return currentSelection;
    }

    public void setCurrentSelection(InstanceWidget currentSelection) {
        this.currentSelection.set(currentSelection);
    }
}
