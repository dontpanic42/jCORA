package models.datatypes;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen Long-Wert
 */
public class LongValue implements TypedValue<Long> {

    private Long value = 0l;

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
            value = Long.parseLong(val);
        } catch (NumberFormatException e) {

        }
    }

    @Override
    public void setFromLiteral(Literal literal) {
        value = literal.getLong();
    }

    @Override
    public Long getValue() {
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
