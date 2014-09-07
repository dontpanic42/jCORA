package services.retrieval.similarity.functions.numeric;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import models.cbr.CoraCaseBaseImpl;
import models.datatypes.TypedValue;
import models.ontology.CoraPropertyModel;
import services.retrieval.similarity.functions.SimilarityFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 03.09.14.
 *
 * Abstrakte numerische Ähnlichkeitsfunktion, die die einige für numerische Berechnungen notwendige
 * hilfs-Methoden enthält sowie die Metrik auf basis von <code>BigDecimal</code>s.
 */
public abstract class NumericSimilarityFunction<T extends TypedValue> extends SimilarityFunction<T> {

    /**
     * Definiert ein Extremierungsziel (min. Wert und max. Wert)
     */
    private enum Extrema {
        MIN,
        MAX
    }

    /**
     * Gibt den Minimalwert aus sowohl der Domain-Ontologie als auch der Case-Base zurück, oder TypedValue.getMaxValue(),
     * wenn das Minimum gesucht, aber nicht gefunden wird (d.h. wenn keine Werte gefunden wurden).
     * @param property Die betreffende Eigenschaft
     * @param valueType Die Klasse des Ziel-Werttypes
     * @param <T> Der Ziel-Werttyp
     * @return Das Minimum aus Domain-Ontologie und Case-Base
     */
    protected <T extends TypedValue> T getGlobalMinValue(CoraPropertyModel property, Class<T> valueType) {
        return getGlobalExtrema(property, valueType, Extrema.MIN);
    }

    /**
     * Gibt den Maximalwert aus sowohl der Domain-Ontologie als auch der Case-Base zurück, oder TypedValue.getMinValue(),
     * wenn das Maximum gesucht, aber nicht gefunden wird (d.h. wenn keine Werte gefunden wurden).
     * @param property Die betreffende Eigenschaft
     * @param valueType Die Klasse des Ziel-Werttypes
     * @param <T> Der Ziel-Werttyp
     * @return Das Maximum aus Domain-Ontologie und Case-Base
     */
    protected <T extends TypedValue> T getGlobalMaxValue(CoraPropertyModel property, Class<T> valueType) {
        return getGlobalExtrema(property, valueType, Extrema.MAX);
    }

    /**
     * Gibt einen Extremwert aus sowohl der Domain-Ontologie als auch der Case-Base zurück, oder:
     *  - TypedValue.getMaxValue(), wenn das Minimum gesucht, aber nicht gefunden wird
     *  - TypedValue.getMinValue(), wenn das Maximum gesucht, aber nicht gefunden wird
     * @param property Die betreffende Eigenschaft
     * @param valueType Die Klasse des Ziel-Werttypes
     * @param goal Das extremierungsziel (min/max)
     * @param <T> Der Ziel-Werttyp
     * @return Der Extremwert
     */
    private <T extends TypedValue> T getGlobalExtrema(CoraPropertyModel property, Class<T> valueType, Extrema goal) {
        List<T> valuesFromDomain = getValuesFromDomain(property, valueType);
        List<T> valuesFromCaseBase = getValuesFromCaseBase(property, valueType);

        // Wenn nur Werte in der Domain gefunden wurden, gib das Domain-Extrem zurück
        if(valuesFromDomain.size() > 0 && valuesFromCaseBase.size() == 0) {
            return getExtremeValueFromList(valuesFromDomain, valueType, goal);
        }

        // Wenn nur Werte in der Case-Base gefunden wurden, gibt das Case-Base-Extrem zurück.
        if(valuesFromCaseBase.size() > 0 && valuesFromDomain.size() == 0) {
            return getExtremeValueFromList(valuesFromCaseBase, valueType, goal);
        }

        // Wenn weder in der Case-Base noch in der Domain Werte gefunden wurden, gib maxValue
        // zurück, wenn das Minimum gesucht wird bzw. minValue, wenn das Maximum gesucht wird.
        if(valuesFromCaseBase.size() == 0 && valuesFromDomain.size() == 0) {
            try {
                return (T) ((goal == Extrema.MIN)?  valueType.newInstance().getMaxValue():
                        valueType.newInstance().getMinValue());
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        TypedValue tcb = getExtremeValueFromList(valuesFromCaseBase, valueType, goal);
        TypedValue tdo = getExtremeValueFromList(valuesFromDomain, valueType, goal);

        // Wenn in beiden Werte gefunden wurden, gib den global größten bzw. kleinsten zurück.
        if(goal == Extrema.MIN) {
            return (T) ((tcb.compareTo(tdo) < 0)? tcb : tdo);
        } else {
            return (T) ((tcb.compareTo(tdo) > 0)? tcb : tdo);
        }
    }

    /**
     * Gibt einen Extremwert aus einer Liste mit Werten zurück.
     * @param inputList Die Eingableliste
     * @param valueType Die Klasse des Ziel-Wertetyp (Class\<\? extends TypedValue>)
     * @param goal Das extremierungsziel (min/max)
     * @param <T> Der Zieltyp
     * @return Der Extremwert
     */
    private <T extends TypedValue> T getExtremeValueFromList(List<T> inputList, Class<T> valueType, Extrema goal) {
        try {
            TypedValue result = (goal == Extrema.MAX)?
                    valueType.newInstance().getMinValue():
                    valueType.newInstance().getMaxValue();

            if(result == null) {
                System.err.println("Fehler: Null! " + goal + " Klasse: " + valueType.getSimpleName());
            }

            int tmp;
            for(T v : inputList) {
                if(v.getClass() == valueType) {
                    tmp = v.compareTo(result);
                    if( (goal == Extrema.MAX && tmp > 0) ||
                            (goal == Extrema.MIN && tmp < 0)) {
                        result = v;
                    }
                }
            }

            return (T) result;
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Gibt alle Werte für ein (Data-)Property aus der Fallbasis (Task-Ontologien) zurück.
     * @param prop Das Property
     * @param valueType Die Klasse des Ziel-Wertetyp (Class\<? extends TypedValue\>>)
     * @param <T> Der Zielwertetyp (abgeleitet von TypedValue)
     * @return Eine Liste mit TypedValues
     */
    private <T extends TypedValue> List<T> getValuesFromCaseBase(CoraPropertyModel prop, Class<T> valueType) {
        String propName = prop.getBaseObject().getNameSpace() + prop.getBaseObject().getLocalName();

        String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT DISTINCT ?value \n" +
                "WHERE \n" +
                "  { \n" +
                "     GRAPH ?g {?y <" + propName + "> ?value}\n" +
                "  }";

        Query jquery = QueryFactory.create(query);

        CoraCaseBaseImpl cbi = (CoraCaseBaseImpl) getFactory().getCaseBase();
        Dataset d = cbi.getDataset();

        d.begin(ReadWrite.READ);
        QueryExecution qe = QueryExecutionFactory.create(query, d);

        ResultSet resultSet = qe.execSelect();

        List<T> literals = new ArrayList<>();
        while(resultSet.hasNext()) {
            RDFNode soln = resultSet.next().get("value");
            if(soln.isLiteral()) {
                try {
                    T typedValue = valueType.newInstance();
                    typedValue.setFromLiteral(soln.asLiteral());
                    literals.add(typedValue);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        d.end();

        return literals;
    }

    /**
     * Gibt alle Werte für ein (Data-)Property aus der Domain-Ontologie zurück.
     * @param prop Das Property
     * @param valueType Die Klasse des Ziel-Wertetyp (Class\<? extends TypedValue\>>)
     * @param <T> Der Zielwertetyp (abgeleitet von TypedValue)
     * @return Eine Liste mit TypedValues
     */
    private <T extends TypedValue> List<T> getValuesFromDomain(CoraPropertyModel prop, Class<T> valueType) {
        String propName = prop.getBaseObject().getNameSpace() + prop.getBaseObject().getLocalName();
        String query =  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT DISTINCT ?value\n" +
                "WHERE { ?y <" + propName + "> ?value }";

        Query jquery = QueryFactory.create(query);

        QueryExecution qe = QueryExecutionFactory.create(query, prop.getModel());

        ResultSet resultSet = qe.execSelect();

        //ResultSetFormatter.out(System.out, resultSet, jquery);

        List<T> literals = new ArrayList<>();
        while(resultSet.hasNext()) {
            RDFNode soln = resultSet.next().get("value");
            if(soln.isLiteral()) {
                try {
                    T typedValue = valueType.newInstance();
                    typedValue.setFromLiteral(soln.asLiteral());
                    literals.add(typedValue);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return literals;
    }

    /**
     * Berechnet die Ähnlichkeit zweier Werte <code>aVal</code> und <code>bVal</code> unter Berücksichtigung der
     * des Abstands von <code>maxVal</code> und <code>minVal</code>. Für die Ähnlichkeitsberechnung wird vorausgesetzt,
     * das die Bedingung <code>maxVal >= minVal</code> erfüllt ist.
     * @param aVal Der Eine Wert
     * @param bVal Der Andere Wert
     * @param maxVal Die global-maximale Werteausprägung
     * @param minVal Die global-minimal Werteausprägung
     * @return Die Ähnlichkeit der Werte im Interval 0..1
     */
    protected float getMetricSimilarity(BigDecimal aVal, BigDecimal bVal, BigDecimal maxVal, BigDecimal minVal) {
        BigDecimal nen = ( (aVal.subtract(bVal)).abs() );
        BigDecimal zae = ( (maxVal.subtract(minVal)).abs() );

        //@see http://stackoverflow.com/questions/10950914/how-to-check-if-bigdecimal-variable-0-in-java
        if(nen.compareTo(BigDecimal.ZERO) == 0 || zae.compareTo(BigDecimal.ZERO) == 0) {
            return 1.f;
        }

        float sim = (BigDecimal.ONE.subtract( nen.divide(zae, 12, RoundingMode.HALF_UP) )).floatValue();

        //Nur um sicher zu gehen: Beschneide den Wert auf das Interval 0..1
        return Math.min(Math.max(0.f, sim), 1.f);
    }
}
