package factories.ontology;

import com.hp.hpl.jena.ontology.*;
import models.ontology.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by daniel on 20.08.14.
 */
public class CoraOntologyModelFactory {

    private OntModel model;

    public CoraOntologyModelFactory(OntModel model) {
        this.model = model;
    }

    public CoraClassModel wrapClass(OntClass clazz) {
        CoraClassModel model = new CoraClassModel();
        model.setBaseObject(clazz);
        model.setModel(this.model);
        model.setFactory(this);

        return model;
    }

    public CoraInstanceModel wrapInstance(Individual indv) {
        CoraInstanceModel model = new CoraInstanceModel();
        model.setBaseObject(indv);
        model.setModel(this.model);
        model.setFactory(this);

        return model;
    }

    public CoraObjectPropertyModel wrapObjectProperty(ObjectProperty prop) {
        CoraObjectPropertyModel model = new CoraObjectPropertyModel();
        model.setBaseObject(prop);
        model.setModel(this.model);
        model.setFactory(this);

        return model;
    }

    public CoraDataPropertyModel wrapDataProperty(DatatypeProperty prop) {
        CoraDataPropertyModel model = new CoraDataPropertyModel();
        model.setBaseObject(prop);
        model.setModel(this.model);
        model.setFactory(this);

        return model;
    }

    public CoraPropertyModel<?> wrapProperty(OntProperty prop) {
        if(prop.canAs(DatatypeProperty.class)) {
            return wrapDataProperty(prop.as(DatatypeProperty.class));
        } else if(prop.canAs(ObjectProperty.class)) {
            return wrapObjectProperty(prop.as(ObjectProperty.class));
        }

        throw new NotImplementedException();
    }
}
