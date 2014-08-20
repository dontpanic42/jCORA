package models.ontology;

import com.hp.hpl.jena.ontology.ObjectProperty;

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
}
