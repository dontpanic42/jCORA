package models.datatypes.xsd;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import models.datatypes.TypedValue;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen Integer wert
 */
public class IntegerValue extends TypedValue<Integer> {

    public IntegerValue() {
        setValue(0);
    }

    public IntegerValue(Integer value) {
        setValue(value);
    }

    @Override
    public boolean isValidString(String val) {
        try {
            Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    @Override
    public void setFromString(String val) {
        try {
            setValue(Integer.parseInt(val));
        } catch (NumberFormatException e) {
            System.err.println("Parsing-Fehler: [" + val + "] ist kein valider Integer");
        }
    }

    @Override
    public Literal getLiteral(OntModel model) {
        return model.createTypedLiteral(getValue());
    }

    @Override
    public TypedValue<Integer> getMaxValue() {
        return new IntegerValue(Integer.MAX_VALUE);
    }

    @Override
    public TypedValue<Integer> getMinValue() {
        return new IntegerValue(Integer.MIN_VALUE);
    }

    @Override
    public String getUnitName() {
        return "(Integer)";
    }
}
