package models.datatypes;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen Float-Wert
 */
public class FloatValue implements TypedValue<Float> {

    private Float value = 0.f;

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
            value = Float.parseFloat(val);
        } catch (NumberFormatException e) {

        }
    }

    @Override
    public void setFromLiteral(Literal literal) {
        value = literal.getFloat();
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public Literal getLiteral(OntModel model) {
        return model.createTypedLiteral(value);
    }

    @Override
    public String getAsString() {
        return value.toString();
    }
}
