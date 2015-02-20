package models.datatypes.custom;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import models.datatypes.TypedValue;

/**
 * Created by daniel on 31.01.15.
 */
public class TaricValue extends TypedValue<String> {

    public TaricValue() {
        setValue("");
    }

    public TaricValue(String value) {
        setValue(value);
    }

    @Override
    public boolean isValidString(String val) {
        return true;
    }

    @Override
    public void setFromString(String val) {
        setValue(val);
    }

    @Override
    public Literal getLiteral(OntModel model) {
        return model.createTypedLiteral(getValue());
    }

    @Override
    public TypedValue<String> getMaxValue() {
        return null;
    }

    @Override
    public TypedValue<String> getMinValue() {
        return null;
    }

    @Override
    public String getUnitName() {
        return "Zolltarifnummer";
    }
}
