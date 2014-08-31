package services.retrieval.similarity.functions.numeric;

import models.datatypes.TypedValue;
import models.ontology.CoraPropertyModel;
import services.retrieval.similarity.functions.SimilarityFunction;

/**
 * Created by daniel on 31.08.14.
 */
public class SimilarityFloat extends SimilarityFunction<TypedValue<Float>> {

    @Override
    public Float calculateItemSim(CoraPropertyModel property, TypedValue<Float> a, TypedValue<Float> b) {
        return null;
    }
}
