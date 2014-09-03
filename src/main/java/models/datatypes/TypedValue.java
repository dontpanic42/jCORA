package models.datatypes;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen Wert mit einem (ggf. in der Ontologie definierten) Typ. Wird genutzt, um
 * Daten-Attribute in der Ontologie zu lesen, setzten oder zu bearbeiten.
 */
public abstract class TypedValue<T extends Comparable> implements Comparable {

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
    public abstract void setFromLiteral(Literal literal);

    /**
     * Gibt den Wert als String zurück.
     * @return Der Wert als String
     */
    public abstract String getAsString();

    /**
     * Gibt den Wert zurück
     * @return der Wert
     */
    public abstract T getValue();

    /**
     * Gibt den Wert als Literal zurück
     * @param model Der Wert als Literal
     * @return
     */
    public abstract Literal getLiteral(OntModel model);

    public abstract TypedValue<T> getMaxValue();

    public abstract TypedValue<T> getMinValue();

    @Override
    public int compareTo(Object o) {
        if(o.getClass().equals(this.getClass())) {
            TypedValue<T> otv = (TypedValue<T>) o;
            return getValue().compareTo(otv.getValue());
        }

        if(o.getClass().equals(getValue().getClass())) {
            return getValue().compareTo(o);
        }

        throw new RuntimeException();
    }
}
