package models.datatypes;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen String
 */
public class StringValue implements TypedValue<String> {

    private String value = "";

    @Override
    public boolean isValidString(String val) {
        return true;
    }

    @Override
    public void setFromString(String val) {
        value = val;
    }

    @Override
    public void setFromLiteral(Literal literal) {
        value = literal.getString();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Literal getLiteral(OntModel model) {
        return model.createTypedLiteral(value);
    }

    @Override
    public String getAsString() {
        return value;
    }
}
