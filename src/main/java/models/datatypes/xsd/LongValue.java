package models.datatypes.xsd;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import models.datatypes.TypedValue;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen Long-Wert
 */
public class LongValue extends TypedValue<Long> {

    public LongValue() {
        setValue(0l);
    }

    public LongValue(Long value) {
        setValue(value);
    }

    @Override
    public boolean isValidString(String val) {
        try {
            Float.parseFloat(val);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    @Override
    public void setFromString(String val) {
        try {
            setValue(Long.parseLong(val));
        } catch (NumberFormatException e) {

        }
    }

    @Override
    public Literal getLiteral(OntModel model) {
        return model.createTypedLiteral(getValue());
    }

    @Override
    public TypedValue<Long> getMaxValue() {
        return new LongValue(Long.MAX_VALUE);
    }

    @Override
    public TypedValue<Long> getMinValue() {
        return new LongValue(Long.MIN_VALUE);
    }

    @Override
    public String getUnitName() {
        return "(Long)";
    }
}
