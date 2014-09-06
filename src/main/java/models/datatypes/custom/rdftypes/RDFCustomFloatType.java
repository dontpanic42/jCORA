package models.datatypes.custom.rdftypes;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;

/**
 * Created by daniel on 05.09.14.
 *
 * Repräsentiert einen in der Ontologie definierten Datentyp,
 * der als Java-Typ <code>Float</code> dargestellt werden kann.
 */
public class RDFCustomFloatType extends RDFCustomBaseType<Float> {

    public RDFCustomFloatType(String uri) {
        super(uri, Float.class);
    }

    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        try {
            return Float.parseFloat(lexicalForm);
        } catch (NumberFormatException e) {
            throw new DatatypeFormatException();
        }
    }
}
