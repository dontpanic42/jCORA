package services.retrieval.similarity.functions;

import models.cbr.CoraQueryModel;
import models.ontology.CoraPropertyModel;
import services.retrieval.similarity.SimilarityCache;
import services.retrieval.similarity.SimilarityFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by daniel on 31.08.14.
 */
public abstract class SimilarityFunction<T> {

    private SimilarityFactory factory;
    private CoraQueryModel query;

    /**
     * Gibt zurück, ob die Einzelwerte der Berechnungen von <code>calculateItemSim</code> gecached
     * werden dürfen (<code>true</code>) oder nicht (<code>false</code>).
     * @return <code>true</code>, wenn die Ergebnisse gecached werden dürfen
     */
    protected boolean isCacheable() {
        return true;
    }

    /**
     * Berechnet die Ähnlichkeit zweier Sets von Werten.
     * @param a Das Eine Set
     * @param b Das Andere Set
     * @return Der Ähnlichkeitswert
     */
    public Float calculate(CoraPropertyModel property, Set<T> a, Set<T> b) {
        return calculate(property, new ArrayList<>(a), new ArrayList<>(b));
    }

    public Float calculateFromObject(CoraPropertyModel property, List<Object> a, List<Object> b) {
        return calculate(property, (List<T>) a, (List<T>) b);
    }

    /**
     * Berechnet die Ähnlichkeit zweier Listen von Werten.
     * @param a Die Eine Liste
     * @param b Die Andere Liste
     * @return Der summierte Ähnlichkeitswert
     */
    public Float calculate(CoraPropertyModel property, List<T> a, List<T> b) {
        float result = 0.f;

        //Für jedes Element aus der ersten Wertemenge (a), finde das Ähnlichste Element
        //aus der zweiten Wertemenge.

        for(T itemA : a) {
            float max = 0.f;
            for(T itemB : b) {
                max = Math.max(max, calculate(property, itemA, itemB));
            }

            result += max;
        }

        //Für jedes Element aus der zweiten Wertemenge (b), finde das Ähnlichste Element
        //aus der ersten Wertemenge.

        for(T itemB : b) {
            float max = 0.f;
            for(T itemA : a) {
                max = Math.max(max, calculate(property, itemB, itemA));
            }

            result += max;
        }

        //Teile das Ergebnis durch die Anzahl der Elemente in a und b.
        if(result >= 0.f) {
            result = result / (a.size() + b.size());
        }

        return result;
    }

    /**
     * Berechnet die Ähnlichkeit zweier Objekte <code>a</code> und <code>b</code>. Greift
     * auf gechachete Werte zurück, wenn dies durch <code>isCacheable</code> erlaubt
     * wurde.
     * @param a Der Eine Wert
     * @param b Der Andere Wert
     * @return Die Ähnlichkeit
     */
    public Float calculate(CoraPropertyModel property, T a, T b) {
        if(isCacheable()) {
            if(getCache().has(a, b)) {
                return getCache().get(a, b);
            } else {
                float tmp = calculateItemSim(property, a, b);
                getCache().put(a, b, tmp);
                return tmp;
            }
        } else {
            return calculateItemSim(property, a, b);
        }
    }

    /**
     * Von der jeweiligen Ähnlichkeitsfunktion zu implementierende Methode. Berechnet die (ungecachete)
     * Ähnlichkeit zweier Objekte <code>a</code> und <code>b</code>.
     * @param a Das Eine Objekt
     * @param b Das Andere Objekt
     * @return Die Ähnlichkeit
     */
    public abstract Float calculateItemSim(CoraPropertyModel property, T a, T b);

    /**
     * Gibt die Factory zurück, die diese Ähnlichkeitsfunktion erzeugt hat.
     * @return Die Factory
     */
    public SimilarityFactory getFactory() {
        return factory;
    }

    /**
     * (Nur intern verwendet). Setzt die Factory.
     * @param factory Die Factory
     */
    public void setFactory(SimilarityFactory factory) {
        this.factory = factory;
    }

    /**
     * Gibt den verwendeten Cache für Ähnlichkeitsberechnungen zurück.
     * @return Der Cache
     */
    public SimilarityCache getCache() {
        return getFactory().getCache();
    }

    /**
     * Gibt die Query zurück.
     * @return Die Query
     */
    public CoraQueryModel getQuery() {
        return query;
    }

    /**
     * (Nur intern verwendet). Setzt die Query.
     * @param query CBR-Anfrage
     */
    public void setQuery(CoraQueryModel query) {
        this.query = query;
    }
}
