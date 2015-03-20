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
    }


    public static final String COMMAND_REMOVE_EDGE = "removeEdge";

    private JPopupMenu menu;
    private Point point;
    private Widget widget;
    private InstanceGraph scene;

    private EdgeActionHandler actionHandler = null;

    public EdgeMenu(InstanceGraph scene) {
        this.scene = scene;

        this.menu = new JPopupMenu();
        JMenuItem removeEdgeItem = new JMenuItem(ViewBuilder.getInstance().getText("ui.edge_menu.item_remove_relation"));
        removeEdgeItem.setActionCommand(COMMAND_REMOVE_EDGE);
        removeEdgeItem.addActionListener(this);

        this.menu.add(removeEdgeItem);
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
