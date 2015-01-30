package models.datatypes.custom;

import models.datatypes.xsd.IntegerValue;

/**
 * Created by daniel on 30.01.15.
 */
public class PersonenmonateValue extends IntegerValue {

    private static final String TYPE_NAME = "Personenmonate";

    public PersonenmonateValue() {
        super();
    }

    @Override
    public String getUnitName() {
        return TYPE_NAME;
    }
}
