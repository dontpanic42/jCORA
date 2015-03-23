package models.ontology;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by daniel on 20.08.14.
 */
public class CoraObjectPropertyModel extends CoraPropertyModel<ObjectProperty> {
    @Override
    public boolean isObjectProperty() {
        return true;
    }

    @Override
    public boolean isDataProperty() {
        return false;
    }

    @Override
    public CoraObjectPropertyModel asObjectProperty() {
        return this;
    }

    @Override
    public CoraDataPropertyModel asDataProperty() {
        throw new ClassCastException();
    }

    @Override
    public Class<?> getRangeDataType() {
        return CoraInstanceModel.class;
    }

    /**
     * Lists all classes, that are a "valid" range (in the sens of being
     * a subclass of the properties range)
     * @return Liste mit Klassen
     */
    public Set<CoraClassModel> getRangeClasses() {
        Set<CoraClassModel> result = new HashSet<>();

        ExtendedIterator<? extends OntResource> iter = getBaseObject().listRange();
        while(iter.hasNext()) {
            OntResource res = iter.next();
            if(res.canAs(OntClass.class)) {
                result.add(getFactory().wrapClass(res.as(OntClass.class)));
            }
        }

        return result;
    }
}
