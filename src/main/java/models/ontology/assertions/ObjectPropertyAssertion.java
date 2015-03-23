package models.ontology.assertions;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;

/**
 * Created by daniel on 27.01.15.
 */
public class ObjectPropertyAssertion extends Assertion<CoraInstanceModel, CoraObjectPropertyModel, CoraInstanceModel> {
    public ObjectPropertyAssertion(Statement s) {
        super(s);
    }

    @Override
    public CoraInstanceModel getSubject() {
        Statement s = getBaseObject();
        if(s.getSubject().canAs(Individual.class)) {
            return getFactory().wrapInstance(s.getSubject().as(Individual.class));
        }

        return null;
    }

    @Override
    public CoraObjectPropertyModel getPredicat() {
        Statement s = getBaseObject();
        if(s.getPredicate().canAs(ObjectProperty.class)) {
            return getFactory().wrapObjectProperty(s.getPredicate().as(ObjectProperty.class));
        }

        return null;
    }

    @Override
    public CoraInstanceModel getObject() {
        Statement s = getBaseObject();
        RDFNode r = s.getObject();
        if(r.canAs(Individual.class)) {
            return getFactory().wrapInstance(r.as(Individual.class));
        }

        return null;
    }
}
