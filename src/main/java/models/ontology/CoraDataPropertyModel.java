package models.ontology;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import models.ontology.datatypes.DatatypeMapper;

/**
 * Repräsentiert ein Daten-Attribut (DataProperty) aus der Ontologie
 */
public class CoraDataPropertyModel extends CoraPropertyModel<DatatypeProperty> {
    @Override
    public boolean isObjectProperty() {
        return false;
    }

    @Override
    public boolean isDataProperty() {
        return true;
    }

    @Override
    public CoraObjectPropertyModel asObjectProperty() {
        throw new ClassCastException();
    }

    @Override
    public CoraDataPropertyModel asDataProperty() {
        return this;
    }

    @Override
    public Class<?> getRangeDataType() {
        ExtendedIterator<? extends OntResource> iter = getBaseObject().listRange();
        while(iter.hasNext()) {
            Class<?> type = DatatypeMapper.getMapping(iter.next());
            if(type != null) {
                iter.close();
                return type;
            }
        }

        return null;
    }
}
