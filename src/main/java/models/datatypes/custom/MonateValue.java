package models.datatypes.custom;

import models.datatypes.xsd.IntegerValue;

/**
 * Created by daniel on 05.09.14.
 */
public class MonateValue extends IntegerValue {

    private static final String TYPE_NAME = "Monate";

    @Override
    public String getUnitName() {
        return TYPE_NAME;
    }
}
