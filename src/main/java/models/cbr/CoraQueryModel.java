package models.cbr;

/**
 * Created by daniel on 20.08.14.
 */
public class CoraQueryModel {

    private CoraCaseModel queryCase;
    private CoraWeightModel queryWeight;
    private int maxCases;

    /**
     * Erzeugt eine neue CBR-Query aus einem Fall (der ausschließlich die Fallbeschreibung
     * enthält) und den Attributsgewichtungen.
     * @param queryCase Der zu suchende Fall
     * @param queryWeight Die Attributsgewichtungen
     */
    public CoraQueryModel(CoraCaseModel queryCase, CoraWeightModel queryWeight, int maxCases) {
        this.queryCase = queryCase;
        this.queryWeight = queryWeight;
        this.maxCases = maxCases;
    }

    /**
     * Gibt den zu suchenden Fall zurück
     * @return Der Fall
     */
    public CoraCaseModel getCase() {
        return queryCase;
    }

    /**
     * Gibt die Attributsgewichtungen zurück.
     * @return Die Attributsgewichtungen
     */
    public CoraWeightModel getWeights() {
        return queryWeight;
    }

    /**
     * Gibt die maximale Anzahl gefundener Fälle zurück
     * @return
     */
    public int getMaxCases() {
        return maxCases;
    }
}
