package models.ontology;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.HashSet;
import java.util.Set;

/**
 * Repräsentiert ein Konzept (Klasse) aus der Ontologie
 */
public class CoraClassModel extends CoraOntologyModel<OntClass> {

    /**
     * Gibt die Oberklasse(n) der Klasse als flache list zurück.
     * @return Liste der Oberklassen
     */
    public Set<CoraClassModel> getFlattenedParents() {
        Set<CoraClassModel> list = new HashSet<CoraClassModel>();

        ExtendedIterator<OntClass> iter = getBaseObject().listSuperClasses(true);
        while(iter.hasNext()) {
            OntClass c = iter.next();
            getFlattenedClassDescription(c, list);
        }

        if(list.contains(getBaseObject())) {
            list.remove(getBaseObject());
        }

        return list;
    }

    /**
     * Gibt alle von dieser Klasse abgeleiteten Klassen als flache Liste
     * zurück.
     * @return Liste der abgeleiteten Klassen
     */
    public Set<CoraClassModel> getFlattenedChildren() {
        Set<CoraClassModel> list = new HashSet<CoraClassModel>();

        ExtendedIterator<OntClass> iter = getBaseObject().listSubClasses(true);
        while(iter.hasNext()) {
            OntClass c = iter.next();
            getFlattenedClassDescription(c, list);
        }

        if(list.contains(getBaseObject())) {
            list.remove(getBaseObject());
        }

        return list;
    }

    /**
     * Gibt eine Liste aller Instanzen zurück, die von dieser Klasse oder
     * den Unterklassen dieser Klasse abgeleitet wurden
     * @return Liste der Instanzen
     */
    public Set<CoraInstanceModel> getInstances() {
        Set<CoraInstanceModel> individuals = new HashSet<CoraInstanceModel>();

        ExtendedIterator<Individual> iter = getModel().listIndividuals(getBaseObject());
        while(iter.hasNext()) {
            individuals.add(getFactory().wrapInstance(iter.next()));
        }

        return individuals;
    }

    /**
     * Gibt alle Attribute zurück, die diese Klasse als Objekt (domain) erlauben. D.h.
     * alle Attribute, die auf dieser oder einer der Oberklassen definiert worden sind.
     * @return Liste der "erlaubten" Attribute
     */
    public Set<CoraPropertyModel<?>> getProperties() {
        Set<CoraPropertyModel<?>> list = new HashSet<CoraPropertyModel<?>>();

        ExtendedIterator<OntProperty> iter = getBaseObject().listDeclaredProperties(false);
        while(iter.hasNext()) {
            OntProperty p = iter.next();
            CoraPropertyModel coraProp = getFactory().wrapProperty(p);
            if(coraProp != null) {
                list.add(coraProp);
            }
        }

        return list;
    }


}
