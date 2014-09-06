package models.datatypes.custom.rdftypes;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;

/**
 * Created by daniel on 05.09.14.
 */
public class RDFCustomIntegerType extends RDFCustomBaseType<Integer> {

    public RDFCustomIntegerType(String uri) {
        super(uri, Integer.class);
    }

    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        try {
            return Integer.parseInt(lexicalForm);
        } catch (NumberFormatException e) {
            throw new DatatypeFormatException();
        }
    }
}
