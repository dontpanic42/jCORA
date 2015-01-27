package view.graphview.menus;

import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import view.graphview.InstanceGraph;
import view.graphview.InstanceWidget;
import view.viewbuilder.ViewBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by daniel on 23.08.14.
 */
public class NodeMenu implements PopupMenuProvider, ActionListener {

    public interface NodeActionHandler {
        public void onAddNode(InstanceWidget parentWidget);

        public void onDeleteNode(InstanceWidget parentWidget);
    }

    public static final String COMMAND_ADD_NODE = "addNode";

    private JPopupMenu menu;
    private Point point;
    private Widget widget;
    private InstanceGraph scene;

    private NodeActionHandler actionHandler = null;

    public NodeMenu(InstanceGraph scene) {
        this.scene = scene;

        menu = new JPopupMenu("");

        JMenuItem addNodeItem = new JMenuItem(ViewBuilder.getInstance().getText("ui.node_menu.item_add_instance"));
        addNodeItem.setActionCommand(COMMAND_ADD_NODE);
        addNodeItem.addActionListener(this);

        menu.add(addNodeItem);
    }

    public void setActionHandler(NodeActionHandler handler) {
        this.actionHandler = handler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(actionHandler == null) {
            return;
        }

        if(e.getActionCommand().equals(COMMAND_ADD_NODE)) {
            actionHandler.onAddNode( (InstanceWidget) this.widget);
            return;
        }
    }

    @Override
    public JPopupMenu getPopupMenu(Widget widget, Point point) {

        this.point = point;
        this.widget = widget;

        return menu;
    }
}
