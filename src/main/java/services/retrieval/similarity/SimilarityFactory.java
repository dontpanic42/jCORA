package services.retrieval.similarity;

import models.cbr.CoraCaseBase;
import services.retrieval.similarity.functions.SimilarityFunction;

/**
 * Created by daniel on 31.08.14.
 */
public interface SimilarityFactory {

    /**
     * Gibt den Cache für Zwischenberechnungen zurück.
     * @return Der Cache
     */
    public SimilarityCache getCache();

    /**
     * Gibt die CaseBase zurück
     * @return Die CaseBase
     */
    public CoraCaseBase getCaseBase();

    /**
     * Gibt eine zum Datentyp <code>Class\<T\></code> passende Ähnlichkeitsfunktion zurück, oder
     * <code>null</code>, wenn keine solche Ähnlichkeitsfunktion vorhanden ist.
     * @param valueClass Klasse des Datentyps
     * @param <T> Der Datentyp
     * @return Ähnlichkeitsfunktion oder <code>null</code>
     */
    public <T> SimilarityFunction<T> getFunction(Class<T> valueClass);
}
