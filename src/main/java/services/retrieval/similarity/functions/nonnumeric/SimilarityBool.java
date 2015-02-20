package services.retrieval.similarity.functions.nonnumeric;

import models.datatypes.xsd.BooleanValue;
import models.ontology.CoraPropertyModel;
import services.retrieval.similarity.functions.SimilarityFunction;

/**
 * Created by daniel on 30.01.15.
 */
public class SimilarityBool extends SimilarityFunction<BooleanValue> {
    @Override
    public Float calculateItemSim(CoraPropertyModel property, BooleanValue a, BooleanValue b) {
        return (a.getValue() == b.getValue())? 1.f : 0.f;
    }
}
