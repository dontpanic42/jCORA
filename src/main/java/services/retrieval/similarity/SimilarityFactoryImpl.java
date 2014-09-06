package services.retrieval.similarity;

import models.cbr.CoraCaseBase;
import models.cbr.CoraQueryModel;
import models.datatypes.xsd.FloatValue;
import models.datatypes.xsd.IntegerValue;
import models.datatypes.xsd.LongValue;
import models.ontology.CoraClassModel;
import models.ontology.CoraInstanceModel;
import services.retrieval.similarity.functions.SimilarityFunction;
import services.retrieval.similarity.functions.numeric.SimilarityFloat;
import services.retrieval.similarity.functions.numeric.SimilarityInteger;
import services.retrieval.similarity.functions.numeric.SimilarityLong;
import services.retrieval.similarity.functions.ontological.SimilarityClass;
import services.retrieval.similarity.functions.ontological.SimilarityInstance;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by daniel on 31.08.14.
 */
public class SimilarityFactoryImpl implements SimilarityFactory {

    private SimilarityCache cache = new SimilarityCache();
    private CoraCaseBase caseBase;

    /**
     * Enthält die Zuordnung von Datentypen zu Ähnlichkeitsfunktionen
     */
    private final Map<Class<?>, SimilarityFunction<?>> functions =
            new HashMap<Class<?>, SimilarityFunction<?>>() {
        {
            /*
             * Für numerische Objekte
             */
            put(FloatValue.class, new SimilarityFloat());
            put(LongValue.class, new SimilarityLong());
            put(IntegerValue.class, new SimilarityInteger());

            /*
             * Für Ontologie-Objekte
             */
            put(CoraInstanceModel.class, new SimilarityInstance());
            put(CoraClassModel.class, new SimilarityClass());
        }
    };

    /**
     * Konstruktor. Injeziert Abhänigigkeiten in die <code>functions</code>-Map
     */
    public SimilarityFactoryImpl(CoraQueryModel query, CoraCaseBase caseBase) {
        this.caseBase = caseBase;

        for(Map.Entry<Class<?>, SimilarityFunction<?>> e : functions.entrySet()) {
            e.getValue().setFactory(this);
            e.getValue().setQuery(query);
        }
    }

    /**
     * Gibt den Cache für Zwischenberechnungen zurück.
     * @return Der Cache
     */
    public SimilarityCache getCache() {
        return cache;
    }

    /**
     * Gibt eine zum Datentyp <code>Class\<T\></code> passende Ähnlichkeitsfunktion zurück, oder
     * <code>null</code>, wenn keine solche Ähnlichkeitsfunktion vorhanden ist.
     * @param valueClass Klasse des Datentyps
     * @param <T> Der Datentyp
     * @return Ähnlichkeitsfunktion oder <code>null</code>
     */
    public <T> SimilarityFunction<T> getFunction(Class<T> valueClass) {
        if(functions.containsKey(valueClass)) {

            try {
                return (SimilarityFunction<T>) functions.get(valueClass);
            } catch (ClassCastException e) {
                System.err.println("Fehlerhafte zuordnung der Ähnlichkeitsfunktion für: " + valueClass.getSimpleName());
                e.printStackTrace();
                return null;
            }

        }

        return null;
    }

    /**
     * Gibt die Case-Base zurück
     * @return Die Case-Base
     */
    @Override
    public CoraCaseBase getCaseBase() {
        return caseBase;
    }
}
