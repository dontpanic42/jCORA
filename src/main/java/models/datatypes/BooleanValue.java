package models.datatypes;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen bool'schen Wert
 */
public class BooleanValue extends TypedValue<Boolean> {

    private static final String TRUE_STRING = "ja";
    private static final String FALSE_STRING = "nein";

    private Boolean value = false;

    public BooleanValue() { }

    public BooleanValue(Boolean value) {
        this.value = value;
    }

    @Override
    public boolean isValidString(String val) {
        return  val.toLowerCase().equals(TRUE_STRING) ||
                val.toLowerCase().equals(FALSE_STRING);
    }

    @Override
    public void setFromString(String val) {
        if(val.toLowerCase().equals(TRUE_STRING)) {
            value = true;
        } else if(val.toLowerCase().equals(FALSE_STRING)) {
            value = false;
        }
    }

    @Override
    public void setFromLiteral(Literal literal) {
        value = literal.getBoolean();
    }

    @Override
    public String getAsString() {
        return (value)? TRUE_STRING : FALSE_STRING;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public Literal getLiteral(OntModel model) {
        return model.createTypedLiteral(value);
    }

    @Override
    public TypedValue<Boolean> getMaxValue() {
        return null;
    }

    @Override
    public TypedValue<Boolean> getMinValue() {
        return null;
    }
}
