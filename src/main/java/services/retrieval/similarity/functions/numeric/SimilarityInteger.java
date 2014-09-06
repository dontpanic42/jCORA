package services.retrieval.similarity.functions.numeric;

import models.datatypes.xsd.IntegerValue;
import models.ontology.CoraPropertyModel;

import java.math.BigDecimal;

/**
 * Created by daniel on 03.09.14.
 */
public class SimilarityInteger extends NumericSimilarityFunction<IntegerValue> {

    private Integer globalMax = null;
    private Integer globalMin = null;

    @Override
    public Float calculateItemSim(CoraPropertyModel property, IntegerValue a, IntegerValue b) {
        //Globale min/max Werte
        globalMax = (globalMax == null)? getGlobalMaxValue(property, IntegerValue.class).getValue() : globalMax;
        globalMin = (globalMin == null)? getGlobalMinValue(property, IntegerValue.class).getValue() : globalMin;

        //Globale min/max Werte inkl. der Werte aus der Anfrage
        //globalMax/globalMin könnten an dieser stelle Long.minValue/Long.maxValue enthalten, wenn keine
        //Daten gefunden wurden. Das ist jedoch egal, da die Werte in diesem Fall durch die Anfragewerte
        //überschrieben werden...
        final BigDecimal maxVal = BigDecimal.valueOf(Math.max(globalMax, Math.max(a.getValue(), b.getValue())));
        final BigDecimal minVal = BigDecimal.valueOf(Math.min(globalMin, Math.min(a.getValue(), b.getValue())));

        final BigDecimal aVal = BigDecimal.valueOf(a.getValue());
        final BigDecimal bVal = BigDecimal.valueOf(b.getValue());

        float sim = getMetricSimilarity(aVal, bVal, maxVal, minVal);

        System.out.println("Sim " + property + " (" + aVal + ", " + bVal + "): " + sim);

        return sim;
    }
}