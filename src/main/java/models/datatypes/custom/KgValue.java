package models.datatypes.custom;

import models.datatypes.xsd.FloatValue;

/**
 * Created by daniel on 06.09.14.
 */
public class KgValue extends FloatValue {

    private static final String TYPE_NAME = "Kg";

    @Override
    public String getUnitName() {
        return TYPE_NAME;
    }
}
