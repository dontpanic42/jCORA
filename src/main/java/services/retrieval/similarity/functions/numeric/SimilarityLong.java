package services.retrieval.similarity.functions.numeric;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import models.cbr.CoraCaseBaseImpl;
import models.datatypes.FloatValue;
import models.datatypes.LongValue;
import models.datatypes.TypedValue;
import models.ontology.CoraPropertyModel;
import services.retrieval.similarity.functions.SimilarityFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 03.09.14.
 */
public class SimilarityLong extends NumericSimilarityFunction<LongValue> {

    private Long globalMax = null;
    private Long globalMin = null;

    @Override
    public Float calculateItemSim(CoraPropertyModel property, LongValue a, LongValue b) {
        //Globale min/max Werte
        globalMax = (globalMax == null)? getGlobalMaxValue(property, LongValue.class).getValue() : globalMax;
        globalMin = (globalMin == null)? getGlobalMinValue(property, LongValue.class).getValue() : globalMin;

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
