package models.ontology;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.HashSet;
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
}
