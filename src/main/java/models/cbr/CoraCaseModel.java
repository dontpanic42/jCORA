package models.cbr;

import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;
import models.ontology.CoraPropertyModel;
import models.ontology.assertions.DataPropertyAssertion;
import models.ontology.assertions.ObjectPropertyAssertion;

import java.util.List;
import java.util.Set;

/**
 * Created by daniel on 22.08.14.
 */
public interface CoraCaseModel {

    public interface CaseChangeHandler {

        /**
         * Wird aufgerufen, wenn auf diesem Fall eine neue Instanz erzeugt wurde.
         * @see models.ontology.CoraOntologyModelFactory#createInstance(models.ontology.CoraClassModel, String)
         * @param instance Die neue Instanz
         */
        public void onAddInstance(CoraInstanceModel instance);

        /**
         * Wird aufgerufen, wenn auf diesem Fall eine neue Objekt-Relation erzeugt wurde.
         * @see models.ontology.CoraInstanceModel#createObjectRelation(models.ontology.CoraObjectPropertyModel, models.ontology.CoraInstanceModel)
         * @param objectProperty Das Prädikat (ObjectProperty)
         * @param subject Das Subjekt (Instanz)
         * @param object Das Objekt (Instanz)
         */
        public void onCreateObjectRelation( CoraObjectPropertyModel objectProperty,
                                            CoraInstanceModel subject,
                                            CoraInstanceModel object);

        /**
         * Wird aufgerufen, wenn auf diesem Fall eine neue Daten-Relation erzeugt wurde.
         * @see models.ontology.CoraInstanceModel#createDataPropertyAssertion(models.ontology.CoraDataPropertyModel, models.datatypes.TypedValue)
         * @param assertion Das Statement
         */
        public void onCreateDataRelation(DataPropertyAssertion assertion);

        /**
         * Wird aufgerufen, wenn auf diesem Fall eine Daten-Relation entfernt wurde.
         * @see models.ontology.CoraInstanceModel#removePropertyAssertion(models.ontology.assertions.DataPropertyAssertion) (models.ontology.assertions.Assertion)
         * @param assertion Das Statement
         */
        public void onDeleteDataRelation(DataPropertyAssertion assertion);

        /**
         * Wird aufgerufen, wenn auf diesem Fall eine Daten-Relation entfernt wurde.
         * @see models.ontology.CoraInstanceModel#removePropertyAssertion(models.ontology.assertions.ObjectPropertyAssertion) (models.ontology.assertions.Assertion)
         * @param assertion Das Statement
         */
        public void onDeleteObjectRelation(ObjectPropertyAssertion assertion);
    }

    public String getCaseId();

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
     * @return Instanz, die die Fallbewertung repräsentiert
     */
    public CoraInstanceModel getCaseJustification();

    /**
     * Gibt eine Liste mit allen, im Fall (und der Domain-Ontologie) vorhandenen
     * Object- und DataProperties zurück.
     * @return Liste mit allen Object- und Dataproperties
     */
    public List<CoraPropertyModel> listAllProperties();

    /**
     * Schließt die Verwendung dieses Modells ab.
     */
    public void close();

    public Set<CaseChangeHandler> getOnChangeHandlers();

    public void addOnChangeHandler(CaseChangeHandler handler);

    public void removeOnChangeHandler(CaseChangeHandler handler);
}
