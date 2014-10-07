package view.graphview.models;

import javafx.beans.property.SimpleObjectProperty;
import models.ontology.CoraInstanceModel;

/**
 * Created by daniel on 23.08.14.
 */
public class NodeModel {

    private SimpleObjectProperty<CoraInstanceModel> model = new SimpleObjectProperty<>();

    public NodeModel() {
    }

    public CoraInstanceModel getModel() {
        return model.get();
    }

    public SimpleObjectProperty<CoraInstanceModel> modelProperty() {
        return model;
    }

    public void setModel(CoraInstanceModel model) {
        this.model.set(model);
    }
}
