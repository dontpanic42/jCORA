package models.datatypes.custom;

import models.datatypes.xsd.IntegerValue;

/**
 * Created by daniel on 30.01.15.
 */
public class StueckValue extends IntegerValue {

    private static final String TYPE_NAME = "Stück";

    public StueckValue() {
        super();
    }

    @Override
    public String getUnitName() {
        return TYPE_NAME;
    }
}

