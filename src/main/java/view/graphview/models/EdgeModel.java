package view.graphview.models;

/**
 * Created by daniel on 23.08.14.
 */
public class EdgeModel {
    private NodeModel source;
    private NodeModel target;

    private String label;

    public EdgeModel(String label) {
        this.label = label;
    }

    public NodeModel getSource() {
        return source;
    }

    public void setSource(NodeModel source) {
        this.source = source;
    }

    public NodeModel getTarget() {
        return target;
    }

    public void setTarget(NodeModel target) {
        this.target = target;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
