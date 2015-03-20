package models.datatypes.custom;

import models.datatypes.TypedValue;
import models.datatypes.xsd.FloatValue;

/**
 * Created by daniel on 05.09.14.
 */
public class EuroValue extends FloatValue {

    private static final String TYPE_NAME = "Euro";

    @Override
    public String getUnitName() {
        return TYPE_NAME;
    }
}
