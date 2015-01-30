package services.retrieval.similarity.functions.ontological;

import models.datatypes.TypedValue;
import models.ontology.*;
import models.util.Pair;
import services.retrieval.similarity.functions.SimilarityFunction;

import java.util.*;

/**
 * Created by daniel on 31.08.14.
 */
public class SimilarityInstance extends SimilarityFunction<CoraInstanceModel> {
    @Override
    public Float calculateItemSim(CoraPropertyModel property, CoraInstanceModel a, CoraInstanceModel b) {


        //Berechne die Klassenähnlichkeit ------------------------------------------------------------------------------
        SimilarityFunction<CoraClassModel> classSimilarityFunction =
                getFactory().getFunction(CoraClassModel.class);

        float classSimilarity = classSimilarityFunction.calculate(
                null, a.getFlattenedTypes(), b.getFlattenedTypes());

        if(classSimilarity == 0.f) {

            System.out.println("Vergleiche Instanzen " + a + " und " + b + " (" + property + "): 0.0");
            return 0.f;
        }

        //Berechne die Attributsähnlichkeit ----------------------------------------------------------------------------

        Pair<Float, Float> oSim = getObjectPropertySimilarity(a, b);
        Pair<Float, Float> dSim = getDataPropertySimilarity(a, b);

        //Instanzen haben weder Object- noch DataProperties
        if(oSim == null && dSim == null) {
            return classSimilarity;
        }

        Pair<Float, Float> pSim = new Pair<>(0.f, 0.f);
        if(oSim != null) {
            pSim.setFirst(pSim.getFirst() + oSim.getFirst());
            pSim.setSecond(pSim.getSecond() + oSim.getSecond());
        }

        if(dSim != null) {
            pSim.setFirst(pSim.getFirst() + dSim.getFirst());
            pSim.setSecond(pSim.getSecond() + dSim.getSecond());
        }

        float weightedPSim = (pSim.getFirst() != 0.f && pSim.getSecond() != 0.f)?
                pSim.getFirst() / pSim.getSecond() : pSim.getFirst();


        //System.out.println("Vergleiche Instanzen " + a + " und " + b + " (" + property + "): " + (classSimilarity * weightedPSim));
        System.out.println("Vergleiche Instanzen " + a + " und " + b + "( + " + property + ")");
        System.out.println("ksim: " + classSimilarity);
        System.out.println("obj-sim: " + ((oSim == null)? 0.0 : oSim.getFirst() + " (weight: " + oSim.getSecond() + ")"));
        System.out.println("dat-sim: " + ((dSim == null)? 0.0 : dSim.getFirst() + " (weight: " + dSim.getSecond() + ")"));

        return classSimilarity * weightedPSim;
    }

    /**
     * Gibt die Ähnlichkeit der Slots als Paar(Summierte Ähnlichkeit, Summierte Gewichte) zurück.
     * - Wenn beide Instanzen keine Object-Properties besitzten, gebe (1.0, 1.0) zurück.
     * - Wenn eine der beiden Instanzen keine Object-Properties besitzten, gebe (0.0, SummeGewichte) zurück.
     * @param a Instanz a
     * @param b Instanz b
     * @return Summierte Ähnlichkeit und Gewichte der Slots
     */
    private Pair<Float, Float> getObjectPropertySimilarity(CoraInstanceModel a, CoraInstanceModel b) {
        Map<CoraObjectPropertyModel, Set<CoraInstanceModel>> propsA = a.getObjectProperties();
        Map<CoraObjectPropertyModel, Set<CoraInstanceModel>> propsB = b.getObjectProperties();

        if(propsA.size() == 0 && propsB.size() == 0) {
            return null;
        }

        Set<CoraObjectPropertyModel> visited = new HashSet<>();
        Pair<Float, Float> similarity = new Pair<>(0.f, 0.f);

        //Für alle Einträge in A
        for(Map.Entry<CoraObjectPropertyModel, Set<CoraInstanceModel>> ea : propsA.entrySet()) {
            CoraObjectPropertyModel objectProperty = ea.getKey();

            if(!visited.contains(objectProperty)) {
                visited.add(objectProperty);

                //Das Property ist eine gemeinsame Eigenschaft:
                if(propsB.containsKey(objectProperty)) {
                    Pair<Float, Float> tmp = getObjectPropertySimilarity(objectProperty,
                            propsA.get(objectProperty), propsB.get(objectProperty));

                    float weightedSimilarity = tmp.getFirst() * tmp.getSecond();
                    float sumSimilarity = similarity.getFirst() + weightedSimilarity;
                    float sumWeights = tmp.getSecond() + similarity.getSecond();

                    similarity.setFirst(sumSimilarity);
                    similarity.setSecond(sumWeights);
                } else {
                //Das Property ist NUR in A vorhanden
                    float weight = getQuery().getWeights().getWeight(objectProperty);
                    float sumWeights = similarity.getSecond() + weight;

                    similarity.setSecond(sumWeights);
                }
            }
        }


        //Für alle Einträge in B
        for(Map.Entry<CoraObjectPropertyModel, Set<CoraInstanceModel>> eb : propsB.entrySet()) {
            CoraObjectPropertyModel objectProperty = eb.getKey();

            if(!visited.contains(objectProperty)) {
                visited.add(objectProperty);

                //Das Attribut kann keine gemeinsame Eigenschaft sein, diese wurden schon vorher
                //abgearbeitet.

                //Das Property ist NUR in A vorhanden
                float weight = getQuery().getWeights().getWeight(objectProperty);
                float sumWeights = similarity.getSecond() + weight;

                similarity.setSecond(sumWeights);
            }
        }

        return similarity;
    }

    /**
     * Berechnet die Ähnlichkeit für ein ObjectProperty
     * @param prop
     * @param va
     * @param vb
     * @return
     */
    private Pair<Float, Float> getObjectPropertySimilarity(CoraObjectPropertyModel prop,
                                                           Set<CoraInstanceModel> va,
                                                           Set<CoraInstanceModel> vb) {
        float weight = getQuery().getWeights().getWeight(prop);
        SimilarityFunction<CoraInstanceModel> simFunc = getFactory().getFunction(CoraInstanceModel.class);
        float sim = simFunc.calculate(prop, va, vb);

        return new Pair(sim, weight);
    }

    /**
     * Berechnet die Ähnlichkeit aller DataProperties zweier Instanzen
     * @param a Die eine Instanz
     * @param b Die andere Instanz
     * @return die Ähnlichkeit
     */
    private Pair<Float, Float> getDataPropertySimilarity(CoraInstanceModel a, CoraInstanceModel b) {
        Map<CoraDataPropertyModel, List<TypedValue>> propsA = getDataPropertyMap(a);
        Map<CoraDataPropertyModel, List<TypedValue>> propsB = getDataPropertyMap(b);

        if(propsA.size() == 0 && propsB.size() == 0) {
            return null;
        }

        Set<CoraDataPropertyModel> visited = new HashSet<>();
        Pair<Float, Float> similarity = new Pair<>(0.f, 0.f);

        //Für alle Einträge in A
        for(Map.Entry<CoraDataPropertyModel, List<TypedValue>> ea : propsA.entrySet()) {
            CoraDataPropertyModel dataProperty = ea.getKey();

            if(!visited.contains(dataProperty)) {
                visited.add(dataProperty);

                //Das Property ist eine gemeinsame Eigenschaft:
                if(propsB.containsKey(dataProperty)) {
                    Pair<Float, Float> tmp = getDataPropertySimilarity(dataProperty,
                            propsA.get(dataProperty), propsB.get(dataProperty));

                    float weightedSimilarity = tmp.getFirst() * tmp.getSecond();
                    float sumSimilarity = similarity.getFirst() + weightedSimilarity;
                    float sumWeights = tmp.getSecond() + similarity.getSecond();

                    similarity.setFirst(sumSimilarity);
                    similarity.setSecond(sumWeights);
                } else {
                    //Das Property ist NUR in A vorhanden
                    float weight = getQuery().getWeights().getWeight(dataProperty);
                    float sumWeights = similarity.getSecond() + weight;

                    similarity.setSecond(sumWeights);
                }
            }
        }


        //Für alle Einträge in B
        for(Map.Entry<CoraDataPropertyModel, List<TypedValue>> eb : propsB.entrySet()) {
            CoraDataPropertyModel dataProperty = eb.getKey();

            if(!visited.contains(dataProperty)) {
                visited.add(dataProperty);

                //Das Attribut kann keine gemeinsame Eigenschaft sein, diese wurden schon vorher
                //abgearbeitet.

                //Das Property ist NUR in B vorhanden
                float weight = getQuery().getWeights().getWeight(dataProperty);
                float sumWeights = similarity.getSecond() + weight;

                similarity.setSecond(sumWeights);
            }
        }

        return similarity;
    }

    /**
     * Gibt eine Map zurück, die alle Property-Werte einer Instanz, geordnet nach DataProperty
     * enthält.
     * @param a Die Betreffende Instanz
     * @return Die Map
     */
    private Map<CoraDataPropertyModel, List<TypedValue>> getDataPropertyMap(CoraInstanceModel a) {
        List<Pair<CoraDataPropertyModel, TypedValue>> propsList = a.getDataProperties();
        Map<CoraDataPropertyModel, List<TypedValue>> props = new HashMap<>();
        for(Pair<CoraDataPropertyModel, TypedValue> p : propsList) {
            if(!props.containsKey(p.getFirst())) {
                props.put(p.getFirst(), new ArrayList<>());
            }

            props.get(p.getFirst()).add(p.getSecond());
        }

        return props;
    }

    /**
     * Berechnet die Ähnlichkeit für alle Werte EINES DataProperties.
     * @param prop Das DataProperty
     * @param valuesA Die erste Wertemenge
     * @param valuesB Die zweite Wertemenge
     * @return
     */
    private Pair<Float, Float> getDataPropertySimilarity(CoraDataPropertyModel prop,
                                                         List<TypedValue> valuesA,
                                                         List<TypedValue> valuesB) {

        float weight = getQuery().getWeights().getWeight(prop);
        SimilarityFunction<?> simFunc = getFactory().getFunction(valuesA.get(0).getClass());
        if(simFunc == null) {
            System.err.println("--> Ignoriere DataProperty " + prop + ": Keine Ähnlichkeitsfunktion für" + valuesA.get(0).getClass().getSimpleName());
            return new Pair(1.f, 1.f);
        } else {
            System.err.println("Berechne Ähnlichkeit für dataproperty: " + prop);
        }

        List<Object> objListA = new ArrayList<>(valuesA);
        List<Object> objListB = new ArrayList<>(valuesB);

        Float sim = simFunc.calculateFromObject(prop, objListA, objListB);

        return new Pair(sim, weight);
    }
}
