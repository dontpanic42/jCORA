package models.cbr;

import models.ontology.CoraPropertyModel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 20.08.14.
 */
public class CoraWeightModel {

    /**
     * Gewicht, das für nicht angegebene oder unbekannte Attribute verwendet wird.
     */
    public static final Float DEFAULT_PROPERTY_WEIGHT = 1.0f;

    /**
     * Attribut - Gewicht Paare
     */
    public Map<CoraPropertyModel, Float> properties;

    /**
     * Erzeugt eine leere Attribut-Gewicht Liste
     */
    public CoraWeightModel() {
        properties = new HashMap<>();
    }

    /**
     * Erzeugt eine initialize Attribut-Gewicht Liste für alle Attribute,
     * die in dem genannten Fall verwendet werden.
     * @param fromCase Der Ausgangsfall
     */
    public CoraWeightModel(CoraCaseModelImpl fromCase) {
        this();
        throw new NotImplementedException();
    }

    /**
     * Gibt eine Liste mit Attributen und deren Gewichten zurück.
     * @return Liste mit Gewichten
     */
    public Map<CoraPropertyModel, Float> getWeights() {
        return properties;
    }

    /**
     * Gibt das Gewicht für ein gegebenes Attribut zurück. Ist das Gewicht
     * des Attributs vorher nicht explizit gesetzt worden, wird <code>DEFAULT_PROPERTY_WEIGHT</code>
     * zurückgegeben.
     * @param property Das Attribut, für das das Gewicht gesucht wird
     * @return Das festgelegte Gewicht oder <code>DEFAULT_PROPERTY_WEIGHT</code>
     */
    public Float getWeight(CoraPropertyModel property) {
        if(properties.containsKey(property)) {
            return properties.get(property);
        }

        return DEFAULT_PROPERTY_WEIGHT;
    }

    /**
     * Setzt das Gewicht für ein gegebenes Attribut.
     * @param property Das Attribut
     * @param weight Das Gewicht für das Attribut
     */
    public void setWeight(CoraPropertyModel property, float weight) {
        properties.put(property, weight);
    }


}
