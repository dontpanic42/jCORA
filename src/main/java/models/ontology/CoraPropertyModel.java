package models.ontology;

import com.hp.hpl.jena.ontology.OntProperty;

import java.util.Set;

/**
 * Die Klasse repräsentiert ein abstraktes Attribut aus der Ontologie. Dieses kann entweder ein Daten-Attribut
 * oder ein Slot sein.
 */
public abstract class CoraPropertyModel<T extends OntProperty> extends CoraOntologyModel<T> {
    /**
     * Gibt <code>true</code> zurück, wenn dieses Attribut ein Slot ist.
     * @return
     */
    public abstract boolean isObjectProperty();

    /**
     * Gibt <code>true</code> zurück, wenn dieses Attribt ein Daten-Attribut ist.
     * @return
     */
    public abstract boolean isDataProperty();

    /**
     * Gibt dieses Attribut als Slot zurück.
     * @return
     */
    public abstract CoraObjectPropertyModel asObjectProperty();

    /**
     * Gibt dieses Attribut als Daten-Attribut zurück.
     * @return
     */
    public abstract CoraDataPropertyModel asDataProperty();

    /**
     * Gibt den Datentyp des Subjekts zurück. D.h. für Daten-Attribute
     * z.B. <code>Float.class</code> oder <code>Integer.class</code>,
     * für Slots <code>CoraInstanceModel.class</code>
     * @return Datentyp des Subjekts
     */
    public abstract Class<?> getRangeDataType();
}
