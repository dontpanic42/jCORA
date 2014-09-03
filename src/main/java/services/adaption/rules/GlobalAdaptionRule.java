package services.adaption.rules;

import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraRetrievalResult;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * Created by daniel on 03.09.14.
 */
public abstract class GlobalAdaptionRule implements AdaptionRule {
    @Override
    public boolean isLocalAdaptionRule() {
        return false;
    }

    @Override
    public boolean isGlobalAdaptionRule() {
        return true;
    }

    @Override
    public LocalAdaptionRule asLocalAdaptionRule() {
        throw new NotImplementedException();
    }

    @Override
    public GlobalAdaptionRule asGlobalAdaptionRule() {
        return this;
    }

    /**
     * Gibt einen neuen, adaptierten Fall zurück. Grundlage für die Adaption ist das Ergebnis der vorherigen
     * die Query, die möglicherweise schon von einer anderen Adaptionsfunktion bearbeitet wurde.
     * @param query Die originale Query
     * @param selectedCase Der vom Nutzer ausgewählte Fall, an dem sich die Adaption orientiert
     * @param retrievalResultList Die Retrieval-Ergebnisse
     * @param caseBase Die Fallbasis
     * @return Der bearbeitete Fall (<code>prev</code>)
     */
    public abstract void adapt(CoraQueryModel query,
                               CoraCaseModel selectedCase,
                               List<CoraRetrievalResult> retrievalResultList,
                               CoraCaseBase caseBase)
            throws Throwable;
}
