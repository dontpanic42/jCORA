package services.retrieval.similarity.functions.numeric;

import models.datatypes.custom.StueckValue;
import models.ontology.CoraPropertyModel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 30.01.15.
 */
public class SimilarityStueck extends NumericSimilarityFunction<StueckValue> {
    private Map<CoraPropertyModel, Integer> globalMaxMap = new HashMap<>();
    private Map<CoraPropertyModel, Integer> globalMinMap = new HashMap<>();

    private Integer getCachedMin(CoraPropertyModel property) {
        if(globalMinMap.containsKey(property)) {
            return globalMinMap.get(property);
        }

        Integer tmp = getGlobalMinValue(property, StueckValue.class).getValue();
        globalMinMap.put(property, tmp);
        return tmp;
    }

    private Integer getCachedMax(CoraPropertyModel property) {
        if(globalMaxMap.containsKey(property)) {
            return globalMaxMap.get(property);
        }

        Integer tmp = getGlobalMaxValue(property, StueckValue.class).getValue();
        globalMaxMap.put(property, tmp);
        return tmp;
    }

    @Override
    public Float calculateItemSim(CoraPropertyModel property, StueckValue a, StueckValue b) {
        //Globale min/max Werte
        Integer globalMax = getCachedMax(property);
        Integer globalMin = getCachedMin(property);

        //Globale min/max Werte inkl. der Werte aus der Anfrage
        //globalMax/globalMin könnten an dieser stelle Long.minValue/Long.maxValue enthalten, wenn keine
        //Daten gefunden wurden. Das ist jedoch egal, da die Werte in diesem Fall durch die Anfragewerte
        //überschrieben werden...
        final BigDecimal maxVal = BigDecimal.valueOf(Math.max(globalMax, Math.max(a.getValue(), b.getValue())));
        final BigDecimal minVal = BigDecimal.valueOf(Math.min(globalMin, Math.min(a.getValue(), b.getValue())));

        final BigDecimal aVal = BigDecimal.valueOf(a.getValue());
        final BigDecimal bVal = BigDecimal.valueOf(b.getValue());

        return getMetricSimilarity(aVal, bVal, maxVal, minVal);
    }
}
