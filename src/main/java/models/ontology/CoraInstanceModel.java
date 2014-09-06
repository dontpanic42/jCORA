package models.ontology;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import models.cbr.CoraCaseModel;
import models.datatypes.TypedValue;
import models.ontology.datatypes.DatatypeMapper;
import models.ontology.assertions.DataPropertyAssertion;
import models.util.Pair;
import org.mindswap.pellet.jena.PelletInfGraph;

import java.util.*;

/**
 * Repräsentiert eine Instanz (a.k.a Individual) aus der Ontologie
 */
public class CoraInstanceModel extends CoraOntologyModel<Individual> {

    /**
     * Gibt die Typen dieser Instanz ("Oberklassen") als flache Liste
     * zurück.
     * @return Liste der Oberklassen
     */
    public Set<CoraClassModel> getFlattenedTypes() {
        Set<CoraClassModel> list = new HashSet<CoraClassModel>();
        ExtendedIterator<OntClass> iter = getBaseObject().listOntClasses(true);
        while(iter.hasNext()) {
            getFlattenedClassDescription(iter.next(), list);
        }

        return list;
    }

    /**
     * Erzeugt eine neue ObjectProperty relation auf dieser Instanz mit dem
     * Prädikat <code>property</code> und dem Objekt <code>toObjekt</code>
     * @param property Das Prädikat
     * @param toObject Das Objekt
     */
    public void createObjectRelation(CoraObjectPropertyModel property,
                                       CoraInstanceModel toObject) {

        getModel().add( this.getBaseObject(),
                        property.getBaseObject(),
                        toObject.getBaseObject());

        CoraCaseModel caseModel = getFactory().getCase();
        if(caseModel != null) {
            for(CoraCaseModel.CaseChangeHandler handler : caseModel.getOnChangeHandlers()) {
                handler.onCreateObjectRelation(property, this, toObject);
            }
        }
    }

    /**
     * Gibt alle DataProperties und Werte zurück, die mit dieser Instanz als Subjekt
     * definiert sind.
     * @deprecated Nur für die Ähnlichkeitsberechnung
     * @see CoraInstanceModel#getDataPropertyAssertions()
     * @return Eine Liste von DataProperty/Wert-Paaren
     */
    public List<Pair<CoraDataPropertyModel, TypedValue>> getDataProperties() {
        Individual i = getBaseObject();

        List<Pair<CoraDataPropertyModel, TypedValue>> list = new ArrayList<>();
        StmtIterator iter = i.listProperties();
        while(iter.hasNext()) {

            Statement s = iter.next();
            Property p = s.getPredicate();

            //System.out.println("Habe Dataproperty: " + p + ": " + s.getObject());

            if(!p.canAs(DatatypeProperty.class)) {
                continue;
            }

            RDFNode r = s.getObject();
            if(!r.isLiteral()) {
                continue;
            }

            CoraDataPropertyModel property = getFactory().wrapDataProperty(p.as(DatatypeProperty.class));

            //TypedValue value = DatatypeMapper.getValue(r.asLiteral());
            TypedValue value = DatatypeMapper.getTypedValue(property, r.asLiteral());
            if(value == null) {
                continue;
            }



            list.add(new Pair(property, value));
        }

        return list;
    }

    /**
     * Gibt eine Liste mit DataProperty-Assertions zurück, die diese Instanz als Subjekt besitzen.
     * @return Liste der DataProperty-Assertions
     */
    public List<DataPropertyAssertion> getDataPropertyAssertions() {
        Model m = getModel();
        List<DataPropertyAssertion> assertions = new ArrayList<>();
        StmtIterator iter = m.listStatements(getBaseObject(), null, (RDFNode) null);
        while(iter.hasNext()) {
            Statement s = iter.next();
            if(s.getPredicate().canAs(DatatypeProperty.class)) {
                assertions.add(getFactory().wrapDataPropertyStatement(s));
            }
        }

        return assertions;
    }

    /**
     * Erzeugt eine neue DataProperty-Assertion
     * @param predicat
     * @param object
     * @return
     */
    public DataPropertyAssertion createDataPropertyAssertion(CoraDataPropertyModel predicat, TypedValue object) {
        Statement s = getModel().createStatement(this.getBaseObject(),
                predicat.getBaseObject(), object.getLiteral(getModel()));

        getModel().add(s);
        getModel().prepare();

        DataPropertyAssertion assertion = getFactory().wrapDataPropertyStatement(s);

        CoraCaseModel caseModel = getFactory().getCase();
        if(caseModel != null) {
            for(CoraCaseModel.CaseChangeHandler handler : caseModel.getOnChangeHandlers()) {
                handler.onCreateDataRelation(assertion);
            }
        }

        return assertion;
    }

    /**
     * Löscht eine DataProperty-Assertion
     * @param assertion Das Statement
     */
    public void removePropertyAssertion(DataPropertyAssertion assertion) {
        getModel().remove(assertion.getBaseObject());
        getModel().prepare();

        // Lade das Pellet-Inferenz-Modell komplett neu, da sonst die
        // 'remove' änderungen erst bei neu Laden des Falles übernommen werden...
        if(getModel().getGraph() instanceof PelletInfGraph) {
            PelletInfGraph pg = (PelletInfGraph) getModel().getGraph();
            pg.reload();
        }

        CoraCaseModel caseModel = getFactory().getCase();
        if(caseModel != null) {
            for(CoraCaseModel.CaseChangeHandler handler : caseModel.getOnChangeHandlers()) {
                handler.onDeleteDataRelation(assertion);
            }
        }
    }

    /**
     * Git eine <code>Map</code> mit <code>CoraObjectProperty</code> (slot) und
     * <cdoe>CoraInstanceModel</cdoe> (Instanzen) zurück, die die ObjectProperties
     * dieser Instanz bezeichnen.
     * @return <code>Map</code> mit ObjectPropertys und zugehörigen Werten
     */
    public Map<CoraObjectPropertyModel, Set<CoraInstanceModel>> getObjectProperties() {
        Individual i = getBaseObject();

        Map<CoraObjectPropertyModel, Set<CoraInstanceModel>> list =
                new HashMap<>();

        StmtIterator iter = i.listProperties();
        while(iter.hasNext()) {

            Statement s = iter.next();
            Property p = s.getPredicate();
            //System.out.println("Habe property: " + p + ": " + s.getObject());

            if(!p.canAs(ObjectProperty.class)) {
                continue;
            }

            RDFNode r = s.getObject();
            if(!r.canAs(Individual.class)) {
                continue;
            }

            CoraObjectPropertyModel property = getFactory().wrapObjectProperty(p.as(ObjectProperty.class));
            CoraInstanceModel instance = getFactory().wrapInstance(r.as(Individual.class));

            if(!list.containsKey(property)) {
                Set<CoraInstanceModel> set = new HashSet<>();
                list.put(property, set);
            }

            list.get(property).add(instance);
        }

        return list;
    }

    /**
     * Gibt eine Liste mit hinzufügbaren OjectProperties zurück (abhängig vom Typ der Instanz)
     * @return Liste mit "erlaubten" ObjectProperties.
     */
    public Set<CoraObjectPropertyModel> getAvailableObjectProperties() {
        Set<CoraClassModel> types = getFlattenedTypes();
        Set<CoraObjectPropertyModel> results = new HashSet<>();

        for(CoraClassModel clazz : types) {
            Set<CoraPropertyModel<?>> typeProperties = clazz.getProperties();
            for(CoraPropertyModel p : typeProperties) {
                if(p.isObjectProperty()) {
                    results.add(p.asObjectProperty());
                }
            }
        }

        return results;
    }

    /**
     * Gibt eine Liste mit hinzufügbaren DataProperties zurück (abhängig vom Typ der Instanz)
     * @return Liste mit "erlaubten" DataProperties.
     */
    public Set<CoraDataPropertyModel> getAvailableDataProperties() {
        Set<CoraClassModel> types = getFlattenedTypes();
        Set<CoraDataPropertyModel> results = new HashSet<>();

        for(CoraClassModel clazz : types) {
            Set<CoraPropertyModel<?>> typeProperties = clazz.getProperties();
            for(CoraPropertyModel p : typeProperties) {
                if(p.isDataProperty()) {
                    results.add(p.asDataProperty());
                }
            }
        }

        return results;
    }
}
