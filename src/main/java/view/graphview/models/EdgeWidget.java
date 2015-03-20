package view.graphview.models;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * Created by daniel on 27.01.15.
 */
public class EdgeWidget extends ConnectionWidget {

    private EdgeModel model;

    public EdgeWidget(EdgeModel model, Scene scene) {
        super(scene);
        this.model = model;
    }

    public EdgeModel getModel() {
        return model;
    }

    public void setModel(EdgeModel model) {
        this.model = model;
    }
}
