package models.datatypes;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Created by daniel on 23.08.14.
 *
 * Repräsentiert einen Wert mit einem (ggf. in der Ontologie definierten) Typ. Wird genutzt, um
 * Daten-Attribute in der Ontologie zu lesen, setzten oder zu bearbeiten.
 */
public interface TypedValue<T> {

    /**
     * Gibt zurück, ob der String <code>val</code> eine gülitge Repräsentation des Wertes
     * darstellt.
     * @param val Mögliche Stringrepräsentation des Wertes
     * @return <code>true</code>, wenn der String als Wert geparst werden kann
     */
    public boolean isValidString(String val);

    /**
     * Setzt den Wert, ausgehend von einem String
     * @param val Stringrepräsentation des Wertes
     */
    public void setFromString(String val);

    /**
     * Setzt den Wert durch ein Literal
     * @param literal Wert als Literal
     */
    public void setFromLiteral(Literal literal);

    /**
     * Gibt den Wert als String zurück.
     * @return Der Wert als String
     */
    public String getAsString();

    /**
     * Gibt den Wert zurück
     * @return der Wert
     */
    public T getValue();

    /**
     * Gibt den Wert als Literal zurück
     * @param model Der Wert als Literal
     * @return
     */
    public Literal getLiteral(OntModel model);
}
