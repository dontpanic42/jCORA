package models.datatypes;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen Integer wert
 */
public class IntegerValue extends TypedValue<Integer> {

    Integer value = 0;

    public IntegerValue() { }

    public IntegerValue(Integer value) {
        this.value = value;
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
            value = Integer.parseInt(val);
        } catch (NumberFormatException e) {

        }
    }

    @Override
    public void setFromLiteral(Literal literal) {
        value = literal.getInt();
    }

    @Override
    public Integer getValue() {
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

    @Override
    public TypedValue<Integer> getMaxValue() {
        return new IntegerValue(Integer.MAX_VALUE);
    }

    @Override
    public TypedValue<Integer> getMinValue() {
        return new IntegerValue(Integer.MIN_VALUE);
    }
}
