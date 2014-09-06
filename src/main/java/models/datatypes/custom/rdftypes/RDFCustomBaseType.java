package models.datatypes.custom.rdftypes;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * Abstrakte Klasse, die einen Anwendungsspezifischen RDF-Datentyp repräsentiert.
 * Dabei wird angenommen, das dieser Typ auf einem bekannten (XSD-)Datentyp basiert,
 * z.B. basiert der Typ <code>Euro</code> auf dem XSD-Typ <code>Float</code>.
 * @param <T> Der zugrundeliegende Datentyp
 */
public abstract class RDFCustomBaseType<T> extends BaseDatatype {

    /**
     * Konstruktor.
     * @param uri Die (volle) URI der Datentyp-Definition
     * @param type Der Basistyp (z.B. bei Euro: Float, TARIC: String etc.)
     */
    public RDFCustomBaseType(String uri, Class<T> type) {
        super(uri);
    }

    /**
     * Wert vom Basis-Typ -> RDF-Stringdarstellung
     * @param value
     * @return
     */
    @Override
    public String unparse(Object value) {
        return (value == null)? "null" : value.toString();
    }

    /**
     * RDF-Stringdarstellung -> Basistyp
     * @param lexicalForm
     * @return
     * @throws DatatypeFormatException
     */
    @Override
    public abstract Object parse(String lexicalForm) throws DatatypeFormatException;

    /**
     * Gibt zurück, ob die Literals <code>v1</code> und <code>v2</code> gleich sind.
     * @param v1 Das eine Literal
     * @param v2 Das andere Literal
     * @return <code>true</code> wenn gleich, sonst <code>false</code>
     */
    @Override
    public boolean isEqual(LiteralLabel v1, LiteralLabel v2) {
        return  v1.getDatatype() == v2.getDatatype() &&
                v1.getValue().equals(v2.getValue());
    }
}
