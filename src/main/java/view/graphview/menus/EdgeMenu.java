package view.graphview.menus;

import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import view.graphview.InstanceGraph;
import view.graphview.InstanceWidget;
import view.viewbuilder.ViewBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by daniel on 27.01.15.
 */
public class EdgeMenu implements PopupMenuProvider, ActionListener {
    public interface EdgeActionHandler {
        public void onDeleteEdge(ConnectionWidget edgeWidget);

        public void onDeleteEdgeRecursive(ConnectionWidget edgeWidget);
    }


    public static final String COMMAND_REMOVE_EDGE = "removeEdge";
    public static final String COMMAN_REMOVE_EDGE_RECURSIVE = "removeEdgeRecursive";

    private JPopupMenu menu;
    private Point point;
    private Widget widget;
    private InstanceGraph scene;

    private EdgeActionHandler actionHandler = null;

    public EdgeMenu(InstanceGraph scene) {
        this.scene = scene;

        this.menu = new JPopupMenu();
        final String removeEdgeText = ViewBuilder.getInstance().getText("ui.edge_menu.item_remove_relation");
        JMenuItem removeEdgeItem = new JMenuItem(removeEdgeText);
        removeEdgeItem.setActionCommand(COMMAND_REMOVE_EDGE);
        removeEdgeItem.addActionListener(this);

        final String removeEdgeRecursiveText = ViewBuilder.getInstance().getText("ui.edge_menu.item_remove_relation_recursive");
        JMenuItem removeEdgeItemRecursive = new JMenuItem(removeEdgeRecursiveText);
        removeEdgeItemRecursive.setActionCommand(COMMAN_REMOVE_EDGE_RECURSIVE);
        removeEdgeItemRecursive.addActionListener(this);

        this.menu.add(removeEdgeItem);
        this.menu.add(removeEdgeItemRecursive);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Triggered");
        if(actionHandler == null) {
            return;
        }

        if(e.getActionCommand().equals(COMMAND_REMOVE_EDGE)) {
            System.out.println("Calling event handler");
            actionHandler.onDeleteEdge((ConnectionWidget) this.widget);
            return;
        }

        if(e.getActionCommand().equals(COMMAN_REMOVE_EDGE_RECURSIVE)) {
            System.out.println("Calling event handler");
            actionHandler.onDeleteEdgeRecursive((ConnectionWidget) this.widget);
            return;
        }
    }

    public EdgeActionHandler getActionHandler() {
        return actionHandler;
    }

    public void setActionHandler(EdgeActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    @Override
    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        this.point = point;
        this.widget = widget;

        return menu;
    }
}
