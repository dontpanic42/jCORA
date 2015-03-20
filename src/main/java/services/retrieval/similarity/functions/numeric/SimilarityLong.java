package services.retrieval.similarity.functions.numeric;

import models.datatypes.xsd.FloatValue;
import models.datatypes.xsd.LongValue;
import models.ontology.CoraPropertyModel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 03.09.14.
 */
public class SimilarityLong extends NumericSimilarityFunction<LongValue> {
    private Map<CoraPropertyModel, Long> globalMaxMap = new HashMap<>();
    private Map<CoraPropertyModel, Long> globalMinMap = new HashMap<>();

    private Long getCachedMin(CoraPropertyModel property) {
        if(globalMinMap.containsKey(property)) {
            return globalMinMap.get(property);
        }

        Long tmp = getGlobalMinValue(property, LongValue.class).getValue();
        globalMinMap.put(property, tmp);
        return tmp;
    }

    private Long getCachedMax(CoraPropertyModel property) {
        if(globalMaxMap.containsKey(property)) {
            return globalMaxMap.get(property);
        }

        Long tmp = getGlobalMaxValue(property, LongValue.class).getValue();
        globalMaxMap.put(property, tmp);
        return tmp;
    }

    @Override
    public Float calculateItemSim(CoraPropertyModel property, LongValue a, LongValue b) {
        //Globale min/max Werte

        long globalMax = getCachedMax(property);
        long globalMin = getCachedMin(property);

        //Globale min/max Werte inkl. der Werte aus der Anfrage
        //globalMax/globalMin könnten an dieser stelle Long.minValue/Long.maxValue enthalten, wenn keine
        //Daten gefunden wurden. Das ist jedoch egal, da die Werte in diesem Fall durch die Anfragewerte
        //überschrieben werden...
        final BigDecimal maxVal = BigDecimal.valueOf(Math.max(globalMax, Math.max(a.getValue(), b.getValue())));
        final BigDecimal minVal = BigDecimal.valueOf(Math.min(globalMin, Math.min(a.getValue(), b.getValue())));

        final BigDecimal aVal = BigDecimal.valueOf(a.getValue());
        final BigDecimal bVal = BigDecimal.valueOf(b.getValue());

        float sim = getMetricSimilarity(aVal, bVal, maxVal, minVal);

        //System.out.println("Sim " + property + " (" + aVal + ", " + bVal + ", min: " + globalMin + ", max: " + globalMax + "): " + sim);

        return sim;
    }
}
