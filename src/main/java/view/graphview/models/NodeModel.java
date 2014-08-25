package view.graphview.models;

import models.ontology.CoraInstanceModel;

/**
 * Created by daniel on 23.08.14.
 */
public class NodeModel {
    private String instanceName = "";
    private String instanceType = "";

    private CoraInstanceModel model;

    public NodeModel() {
    }

    public CoraInstanceModel getModel() {
        return model;
    }

    public void setModel(CoraInstanceModel model) {
        this.model = model;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }
}
