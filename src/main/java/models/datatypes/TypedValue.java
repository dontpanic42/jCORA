package models.datatypes;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen Wert mit einem (ggf. in der Ontologie definierten) Typ. Wird genutzt, um
 * Daten-Attribute in der Ontologie zu lesen, setzten oder zu bearbeiten.
 */
public abstract class TypedValue<T extends Comparable> implements Comparable {

    private T value;

    /**
     * Gibt zurück, ob der String <code>val</code> eine gülitge Repräsentation des Wertes
     * darstellt.
     * @param val Mögliche Stringrepräsentation des Wertes
     * @return <code>true</code>, wenn der String als Wert geparst werden kann
     */
    public abstract boolean isValidString(String val);

    /**
     * Setzt den Wert, ausgehend von einem String
     * @param val Stringrepräsentation des Wertes
     */
    public abstract void setFromString(String val);

    /**
     * Setzt den Wert durch ein Literal
     * @param literal Wert als Literal
     */
    public void setFromLiteral(Literal literal) {
        //Benutze hier _nicht_ (z.B.)
        //value = literal.getFloat();
        //damit auch derivative Typen diese Methode nutzen können.
        Object o = literal.getValue();
        if(o instanceof BaseDatatype.TypedValue) {
            BaseDatatype.TypedValue jenaTV = (BaseDatatype.TypedValue) o;
            setFromString(jenaTV.lexicalValue);
        } else if(isValidString(literal.getLexicalForm())) {
            setFromString(literal.getLexicalForm());
        } else {
            System.err.println("Typ-Missmatch: Kann Literal " + literal + " nicht als " + this.getClass().getSimpleName() + " darstellen.");
        }
    }

    /**
     * Gibt den Wert als String zurück.
     * @return Der Wert als String
     */
    public String getAsString() {
        return (value == null)? "(kein Wert)" : value.toString();
    }

    /**
     * Gibt den Wert zurück
     * @return der Wert
     */
    public T getValue() {
        return value;
    }

    /**
     * Setzt den Wert direkt.
     * @param value Der Wert
     */
    protected void setValue(T value) {
        this.value = value;
    }

    /**
     * Gibt den Wert als Literal zurück
     * @param model Das Modell
     * @return Dieser Wert als Literal des Modells <code>model</code>
     */
    public abstract Literal getLiteral(OntModel model);

    /**
     * Gibt den maximalen Wert zurück, den dieses Objekt annehmen kann.
     * @return Ein TypedValue-Objekt, das den größten darzustellenden Wert repräsentiert
     */
    public abstract TypedValue<T> getMaxValue();

    /**
     * Gibt den minimalen Wert zurück, den dieses Objekt annehmen kann. Der Minimale Wert ist
     * dabei (im gegensatz etwa zu <code>Float.MIN_VALUE</code> der absolut kleinste Wert (negativ).
     * @return Ein TypedValue-Objekt, das den kleinsten darzustellenden Wert repräsentiert
     */
    public abstract TypedValue<T> getMinValue();

    /**
     * Gibt den Namen der Einheit zurück, den dieser Wert repräsentiert.
     * Z.B. "Euro", "Meter", "Fließkomma", etc.
     * @return Der Name der Einheit als String
     */
    public String getUnitName() {
        return "(kein Typ)";
    }

    /**
     * Komparator. Gibt das <code>compareTo</code> Ergebnis des Basis-Objekts zurück. Kann mit einem
     * TypedValue-Objekt mit der selben Klasse aufgerufen werden, oder mit einem Wert vom Typ des
     * Basisobjekts
     * @param o Ein anderes TypedValue oder Basis-Objekt
     * @return <0 wenn diese Kleiner, 0 wenn gleich, \>0 wenn dieses Größer
     */
    @Override
    public int compareTo(Object o) {

        if(TypedValue.class.isInstance(o)) {
            TypedValue vOther = (TypedValue) o;
            return getValue().compareTo(vOther.getValue());
        }

        if(o.getClass().equals(getValue().getClass())) {
            return getValue().compareTo(o);
        }

        throw new RuntimeException();
    }
}
