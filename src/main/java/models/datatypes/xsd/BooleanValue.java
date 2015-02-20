package models.datatypes.xsd;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import models.datatypes.TypedValue;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen bool'schen Wert
 */
public class BooleanValue extends TypedValue<Boolean> {

    private static final String TRUE_STRING = "ja";
    private static final String FALSE_STRING = "nein";


    public BooleanValue() {
        setValue(false);
    }

    public BooleanValue(Boolean value) {
        setValue(value);
    }

    @Override
    public boolean isValidString(String val) {
        return  val.toLowerCase().equals(TRUE_STRING) ||
                val.toLowerCase().equals(FALSE_STRING);
    }

    @Override
    public void setFromString(String val) {
        if(val.toLowerCase().equals(TRUE_STRING)) {
            setValue(true);
        } else if(val.toLowerCase().equals(FALSE_STRING)) {
            setValue(false);
        }
    }

    @Override
    public void setFromLiteral(Literal literal) {
        setValue(literal.getBoolean());
    }

    @Override
    public String getAsString() {
        return (getValue())? TRUE_STRING : FALSE_STRING;
    }

    @Override
    public Literal getLiteral(OntModel model) {
        return model.createTypedLiteral(getValue());
    }

    @Override
    public TypedValue<Boolean> getMaxValue() {
        return null;
    }

    @Override
    public TypedValue<Boolean> getMinValue() {
        return null;
    }

    @Override
    public String getUnitName() {
        return "(Boole'scher Wert)";
    }
}
