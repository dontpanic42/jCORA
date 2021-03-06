package models.datatypes.xsd;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import models.datatypes.TypedValue;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen Float-Wert
 */
public class FloatValue extends TypedValue<Float> {

    public FloatValue() {
        setValue(0.f);
    }

    public FloatValue(Float value) {
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
            setValue(Float.parseFloat(val));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Literal getLiteral(OntModel model) {
        return model.createTypedLiteral(getValue());
    }

    @Override
    public TypedValue<Float> getMaxValue() {
        return new FloatValue(Float.MAX_VALUE);
    }

    @Override
    public String getUnitName() {
        return "(Fließkomma)";
    }

    @Override
    public TypedValue<Float> getMinValue() {
        return new FloatValue(-Float.MAX_VALUE);
    }
}
