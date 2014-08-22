package models.cbr;

import models.ontology.CoraInstanceModel;

/**
 * Created by daniel on 22.08.14.
 */
public interface CoraCaseModel {
    /**
     * Gibt die Instanz zurück, die die Wurzel dieses Falles
     * bildet.
     * @return Die Wurzel des Falles
     */
    public CoraInstanceModel getCaseRoot();

    /**
     * Gibt die Instanz zurück, die die Wurzel der Beschreibung dieses Falles
     * bildet.
     * @return Die Wurzel der Fallbeschreibung
     */
    public CoraInstanceModel getCaseDescription();

    /**
     * Gibt die Instanz zurück, die die Wurzel der Lösung dieses Falles bildet.
     * @return Die Wurzel der Falllösung
     */
    public CoraInstanceModel getCaseSolution();

    /**
     * Gibt die Instanz zurück, die die Wurzel der Bewertung dieses Falles bildet.
     * @return
     */
    public CoraInstanceModel getCaseJustification();

    /**
     * Schließt die Verwendung dieses Modells ab.
     */
    public void close();
}
