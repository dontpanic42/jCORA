package services.retrieval.similarity.functions.nonnumeric;

import models.datatypes.custom.TaricValue;
import models.ontology.CoraPropertyModel;
import services.retrieval.similarity.functions.SimilarityFunction;

/**
 * Created by daniel on 31.01.15.
 */
public class SimilarityTaric extends SimilarityFunction<TaricValue> {
    @Override
    public Float calculateItemSim(CoraPropertyModel property, TaricValue a, TaricValue b) {
        String aStr = a.getValue().replaceAll("\\s+", "");
        String bStr = b.getValue().replaceAll("\\s+", "");

        int aLen = aStr.length();
        int bLen = bStr.length();

        if(aLen == 0 || bLen == 0) {
            System.out.println("Sim " + property.toString() + " (" + a.getValue() + ", " + b.getValue() + "): " + 0.f);
            return 0.f;
        }

        /* Integerdivision wird immer abgerundet */
        aLen /= 2;
        bLen /= 2;

        for(int i = 0; i < Math.min(5, Math.min(aLen, bLen)); i++) {
            /* Wenn das Zeichenpaar i und i+1 ungleich ist...*/
            if(aStr.charAt( i*2)    != bStr.charAt( i*2) ||
               aStr.charAt((i*2)+1) != bStr.charAt((i*2)+1)) {
                if(i == 0) {
                    return 0.f;
                } else {
                    /* i ist 0-indexiert */
                    float sim =  ((float) i) / 5.f;
                    System.out.println("Sim " + property.toString() + " (" + a.getValue() + ", " + b.getValue() + "): " + sim);
                    return sim;
                }
            }
        }

        System.out.println("Sim " + property.toString() + " (" + a.getValue() + ", " + b.getValue() + "): " + 1.f);
        return 1.f;
    }
}
