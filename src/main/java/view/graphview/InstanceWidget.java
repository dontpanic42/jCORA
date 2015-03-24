package view.graphview;

import javafx.beans.property.SimpleObjectProperty;
import mainapp.MainApplication;
import models.ontology.CoraClassModel;
import models.ontology.CoraInstanceModel;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;
import view.graphview.models.NodeModel;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Set;

/**
 * Ein Netbeans Visual Library Widget, das eine Instanz repräsentiert
 *
 * Created by daniel on 23.08.14.
 */
public class InstanceWidget extends Widget {

    private LabelWidget instanceName;

    private Scene scene;

    private SimpleObjectProperty<NodeModel> model = new SimpleObjectProperty<>();

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

    private final static Image IMAGE_INSTANCE = ImageUtilities.loadImage("icons/instance-light-16.png");
    private final static Image IMAGE_TYPE = ImageUtilities.loadImage("icons/instance-type-light-16.png");

    private static final Border BORDER_4 = BorderFactory.createEmptyBorder(4);

    private static final Border WIDGET_BORDER = BorderFactory.createLineBorder(2, BORDER_COLOR);
    private static final Border WIDGET_BORDER_SELECTED = BorderFactory.createLineBorder(4, BORDER_COLOR_SELECTED);

    public InstanceWidget(Scene scene) {
        super(scene);
        this.scene = scene;

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

        setOpaque(true);


        model.addListener( (ov, oldModel, newModel ) -> {
            if(newModel != null) {
                final String lang = MainApplication.getInstance().getLanguage();
                instanceName.setLabel(newModel.getModel().getDisplayName(lang));
                setTypeList(newModel.getModel());

                if(newModel.getModel().isPartOfDomainOntology()) {
                    setIsPartOfDomainOntology();
                } else {
                    setIsPartOfTaskOntology();
                }
            } else {
                instanceName.setLabel("");
                setTypeList(null);
            }
        });

        InstanceGraph graph = (InstanceGraph) scene;
        InstanceWidget self = this;
        getActions().addAction(ActionFactory.createSelectAction(new SelectProvider() {
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
                graph.setCurrentSelection(self);
            }
        }));
        graph.currentSelectionProperty().addListener((ov, oldSelection, newSelection) -> setSelected(self == newSelection));
    }

    /**
     * Zeigt die Liste der Klassen an, von denen die Instanz instanziiert wurde.
     * @param instance Die Instanz, deren Klassen angezeigt werden sollen
     */
    private void setTypeList(CoraInstanceModel instance) {
        if(instance == null) {
            return;
        }

        Set<CoraClassModel> s = instance.getFlattenedTypes();
        final String lang = MainApplication.getInstance().getLanguage();
        for(CoraClassModel c : s) {
            addType(c.getDisplayName(lang));
        }
    }

    /**
     * Zeigt die Klasse mit dem Namen <code>type</code> in der Liste der Klassen an,
     * von denen die Instanz instanziiert wurde.
     * @param type Name der Klasse
     */
    private void addType(String type) {
        Widget typeWidget = new Widget(scene);
        typeWidget.setLayout(LayoutFactory.createHorizontalFlowLayout());
        typeWidget.setBorder(BORDER_4);


        ImageWidget typeImage = new ImageWidget(scene);
        typeImage.setImage(IMAGE_TYPE);
        typeWidget.addChild(typeImage);

        LabelWidget instanceType = new LabelWidget(scene);
        instanceType.setFont(scene.getDefaultFont());
        instanceType.setForeground(FOREGROUND_COLOR);
        instanceType.setLabel(type);

        typeWidget.addChild(instanceType);
        addChild(typeWidget);
    }

    /**
     * Ändere die Darstellung dieses Widgets in den "Ausgewählt" Zustand, wenn <code>value</code> <code>true</code> ist,
     * sonst Ändere die Darstellung in den nicht ausgewählten Zustand.
     * @param value Ausgewählt ja/nein
     */
    private void setSelected(boolean value) {
        if(value) {
            setBorder(WIDGET_BORDER_SELECTED);
        } else {
            setBorder(WIDGET_BORDER);
        }
    }

    /**
     * Gibt das zugrundeliegende NodeModel zurück
     * @return das <code>NodeModel</code>
     */
    public NodeModel getModel() {
        return model.get();
    }

    public SimpleObjectProperty<NodeModel> modelProperty() {
        return model;
    }

    /**
     * Setzt das zugrundeliegende <code>NodeModel</code>
     * @param model das <code>NodeModel</code>
     */
    public void setModel(NodeModel model) {
        this.model.set(model);
    }

    /**
     * Setzt das Aussehen des Knotens, wenn die Instanz ein Teil der Domänenontologie ist
     */
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

    /**
     * Setzt das Aussehen des Knotens, wenn die Instanz ein Teil der Anwendungsontologie ist
     */
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
