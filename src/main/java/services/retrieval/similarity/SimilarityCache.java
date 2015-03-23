package services.retrieval.similarity;

import models.util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 31.08.14.
 *
 * Cached Werte aus der Ähnlichkeitsberechnung
 */
public class SimilarityCache {

    private Map<Pair<Object, Object>, Float> cache = new HashMap<>();

    /**
     * Speichert eine Berechnung im Cache.
     * @param modelA Das eine zu vergleichende Objekt
     * @param modelB Das andere zu vergleichende Objekt
     * @param sim Die Ähnlichkeit
     */
    public void put(Object modelA, Object modelB, Float sim) {
        cache.put(new Pair(modelA, modelB), sim);
    }

    /**
     * Gibt zurück, ob eine Ähnlichkeitsberechnung im Cache vorhanden ist.
     * @param modelA Das eine zu vergleichende Objekt
     * @param modelB Das andere zu vergleichende Objekt
     * @return <code>true</code>, wenn die beiden Objekte vorhanden sind
     */
    public boolean has(Object modelA, Object modelB) {
        return cache.containsKey(new Pair(modelA, modelB));
    }

    /**
     * Gibt den gecachten Ähnlichkeitswert für zwei Objekte zurück.
     * @param modelA Das eine zu vergleichende Objekt
     * @param modelB Das andere zu vergleichende Objekt
     * @return der Ähnlichkeitswert
     */
    public Float get(Object modelA, Object modelB) {
        return cache.get(new Pair(modelA, modelB));
    }
}
