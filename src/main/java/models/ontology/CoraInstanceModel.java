package models.ontology;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import models.cbr.CoraCaseModel;
import models.util.Pair;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public void createObjectRelation(CoraObjectPropertyModel property,
                                       CoraInstanceModel toObject) {

        //this.getBaseObject().addProperty(property.getBaseObject(), toObject.getBaseObject());
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
            System.out.println("Habe property: " + p + ": " + s.getObject());

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
}
