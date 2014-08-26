package view.graphview;

import com.sun.javafx.accessible.providers.SelectionProvider;
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
import view.graphview.menus.NodeMenu;
import view.graphview.models.EdgeModel;
import view.graphview.models.NodeModel;

import java.awt.*;

/**
 * Created by daniel on 23.08.14.
 */
public class InstanceGraph extends GraphScene<NodeModel, EdgeModel> implements SelectProvider {


    private LayerWidget mainLayer;
    private LayerWidget connectionLayer;

    private SceneLayout sceneGraphLayout;
    private NodeMenu nodeMenu;

    private InstanceWidget currentSelection;

    public InstanceGraph() {
        super();

        nodeMenu = new NodeMenu(this);

        connectionLayer = new LayerWidget(this);

        mainLayer = new LayerWidget(this);

        addChild(mainLayer);
        addChild(connectionLayer);

        TreeGraphLayout<NodeModel, EdgeModel> graphLayout = new TreeGraphLayout<NodeModel, EdgeModel>(this, 0, 0, 50, 150, false);
        sceneGraphLayout = LayoutFactory.createSceneGraphLayout(this, graphLayout);

        getActions().addAction(ActionFactory.createPanAction());
        getActions().addAction(ActionFactory.createZoomAction());
    }

    public void forceLayout() {
        sceneGraphLayout.invokeLayout();
    }

    public void addConnection(EdgeModel edge) {
        addEdge(edge);

        setEdgeSource(edge, edge.getSource());
        setEdgeTarget(edge, edge.getTarget());
    }

    public NodeMenu getNodeMenu() {
        return nodeMenu;
    }

    @Override
    protected Widget attachNodeWidget(NodeModel nodeModel) {
        InstanceWidget widget = new InstanceWidget(this);
        widget.setInstanceName(nodeModel.getInstanceName());
        widget.setInstanceType(nodeModel.getInstanceType());
        widget.getActions().addAction(ActionFactory.createMoveAction());
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(nodeMenu));
        widget.setNodeModel(nodeModel);

        mainLayer.addChild(widget);
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(EdgeModel edgeModel) {
        ConnectionWidget connectionWidget = new ConnectionWidget(this);

        connectionWidget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);

        LabelWidget edgeLabel = new LabelWidget(this);
        edgeLabel.setOpaque(true);
        edgeLabel.setLabel(edgeModel.getLabel());

        connectionWidget.addChild(edgeLabel);
        connectionWidget.setConstraint(edgeLabel, LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER, 0.5f);

        connectionLayer.addChild(connectionWidget);

        return connectionWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(EdgeModel s, NodeModel oldSourceNode, NodeModel sourceNode) {
        ConnectionWidget c = (ConnectionWidget) findWidget(s);
        Widget source = (Widget) findWidget(sourceNode);
        c.setSourceAnchor(AnchorFactory.createRectangularAnchor(source));
    }

    @Override
    protected void attachEdgeTargetAnchor(EdgeModel s, NodeModel oldTargetNode, NodeModel targetNode) {
        ConnectionWidget c = (ConnectionWidget) findWidget(s);
        Widget target = findWidget(targetNode);
        c.setTargetAnchor(AnchorFactory.createRectangularAnchor(target));
    }

    @Override
    public boolean isAimingAllowed(Widget widget, Point point, boolean b) {
        return false;
    }

    @Override
    public boolean isSelectionAllowed(Widget widget, Point point, boolean b) {
        return true;
    }

    @Override
    public void select(Widget widget, Point point, boolean b) {
        System.out.println("Selection!");

        if(widget == currentSelection) {
            return;
        }

        if(widget instanceof InstanceWidget) {
            if(currentSelection != null) {
                currentSelection.setSelected(false);
            }

            System.out.println("Selected");
            currentSelection = (InstanceWidget) widget;
            currentSelection.setSelected(true);
        } else {
            System.out.println("Selectd: " + widget.getClass().getSimpleName());
        }
    }
}
