package models.datatypes.custom;

import models.datatypes.xsd.FloatValue;

/**
 * Created by daniel on 05.09.14.
 */
public class MeterValue extends FloatValue {

    private static final String TYPE_NAME = "Meter";

    public MeterValue() {
        super();
    }

    @Override
    public String getUnitName() {
        return TYPE_NAME;
    }
}
