package view.graphview;

import mainapp.MainApplication;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.action.SelectAction;
import org.openide.util.Utilities;
import view.graphview.models.NodeModel;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by daniel on 23.08.14.
 */
public class InstanceWidget extends Widget {

    private LabelWidget instanceName;
    private LabelWidget instanceType;

    private NodeModel nodeModel;
    private final static Color FOREGROUND_COLOR = Color.BLACK;

    /**
     * Farbschema für eine Instanz, die zur Task-Ontologie gehört
     */
    private final static Color[] BACKGROUND_COLORS = {
            new Color(224, 237, 245),
            new Color(244, 249, 254)
    };

    /**
     * Farbschema für eine Instanz, die zur Domain-Ontologie gehört
     */
    private final static Color[] BACKGROUND_COLORS_DOMAIN = {
            new Color(255, 235, 214),
            new Color(255, 217, 183)
    };

    private final static Color BORDER_COLOR = new Color(188, 207, 238);
    private final static Color BORDER_COLOR_SELECTED = new Color(255, 143, 13);

    private final static Image IMAGE_INSTANCE = Utilities.loadImage("icons/instance-light-16.png");
    private final static Image IMAGE_TYPE = Utilities.loadImage("icons/instance-type-light-16.png");

    private static final Border BORDER_4 = BorderFactory.createEmptyBorder(4);

    private static final Border WIDGET_BORDER = BorderFactory.createLineBorder(2, BORDER_COLOR);
    private static final Border WIDGET_BORDER_SELECTED = BorderFactory.createLineBorder(4, BORDER_COLOR_SELECTED);

    public InstanceWidget(Scene scene) {
        super(scene);

        setLayout(LayoutFactory.createVerticalFlowLayout());
        setBorder(WIDGET_BORDER);



        Widget instanceNameWidget = new Widget(scene);
        instanceNameWidget.setLayout(LayoutFactory.createHorizontalFlowLayout());
        instanceNameWidget.setBorder(BORDER_4);

        ImageWidget instanceImage = new ImageWidget(scene);
        instanceImage.setImage(IMAGE_INSTANCE);
        instanceNameWidget.addChild(instanceImage);

        instanceName = new LabelWidget(scene);
        instanceName.setFont(scene.getDefaultFont().deriveFont(Font.BOLD));
        instanceName.setForeground(FOREGROUND_COLOR);

        instanceNameWidget.addChild(instanceName);
        addChild(instanceNameWidget);

        Widget instanceTypeWidget = new Widget(scene);
        instanceTypeWidget.setLayout(LayoutFactory.createHorizontalFlowLayout());
        instanceTypeWidget.setBorder(BORDER_4);

        ImageWidget typeImage = new ImageWidget(scene);
        typeImage.setImage(IMAGE_TYPE);
        instanceTypeWidget.addChild(typeImage);

        instanceType = new LabelWidget(scene);
        instanceType.setFont(scene.getDefaultFont());
        instanceType.setForeground(FOREGROUND_COLOR);

        instanceTypeWidget.addChild(instanceType);
        addChild(instanceTypeWidget);

        setOpaque(true);

        InstanceGraph graph = (InstanceGraph) scene;

        if(graph.getSelectProvider() != null) {
            getActions().addAction(ActionFactory.createSelectAction(graph.getSelectProvider()));
        }
    }

    public void setSelected(boolean value) {
        if(value) {
            setBorder(WIDGET_BORDER_SELECTED);
        } else {
            setBorder(WIDGET_BORDER);
        }
    }

    public void setInstanceName(String name) {
        instanceName.setLabel(name);
    }

    public String getInstanceName() {
        return instanceName.getLabel();
    }

    public void setInstanceType(String name) {
        instanceType.setLabel(name);
    }

    public String getInstanceType() {
        return instanceType.getLabel();
    }

    public NodeModel getNodeModel() {
        return nodeModel;
    }

    public void setNodeModel(NodeModel nodeModel) {
        this.nodeModel = nodeModel;

        String instanceNs = nodeModel.getModel().getNs();
        String domainNs = MainApplication.getInstance().getCaseBase().getDomainNs();
        if(instanceNs.equals(domainNs)) {
            setIsPartOfDomainOntology();
        } else {
            setIsPartOfTaskOntology();
        }
    }

    private void setIsPartOfDomainOntology() {
        final float[] gradientFractions = {0.0f, 1.0f};

        LinearGradientPaint lgp = new LinearGradientPaint(
                new Point2D.Double(0, 0),
                new Point2D.Double(0, 40),
                gradientFractions,
                BACKGROUND_COLORS_DOMAIN
        );

        setBackground(lgp);
    }

    private void setIsPartOfTaskOntology() {
        final float[] gradientFractions = {0.0f, 1.0f};

        LinearGradientPaint lgp = new LinearGradientPaint(
                new Point2D.Double(0, 0),
                new Point2D.Double(0, 40),
                gradientFractions,
                BACKGROUND_COLORS
        );

        setBackground(lgp);
    }
}
