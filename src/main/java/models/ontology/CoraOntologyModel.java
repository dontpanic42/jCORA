package models.ontology;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import factories.ontology.CoraOntologyModelFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Set;

/**
 * Diese Klasse repräsentiert ein Element aus der Ontologie
 */
public class CoraOntologyModel<T extends OntResource> {

    private T baseObject;
    private OntModel model;
    private CoraOntologyModelFactory factory;

    public T getBaseObject() {
        return baseObject;
    }

    public void setBaseObject(T baseObject) {
        this.baseObject = baseObject;
    }

    public OntModel getModel() {
        return model;
    }

    public void setModel(OntModel model) {
        this.model = model;
    }

    public String toString() {
        return this.baseObject.getLocalName();
    }

    public String getNs() {
        return this.model.getNsPrefixURI("");
    }

    public CoraOntologyModelFactory getFactory() {
        return factory;
    }

    public void setFactory(CoraOntologyModelFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoraOntologyModel)) return false;

        CoraOntologyModel that = (CoraOntologyModel) o;

        if (!baseObject.equals(that.baseObject)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return baseObject.hashCode();
    }

    /**
     * Reduziert einen komplexen Ausdruck (z.B. <code>subClassOf(A or B)</code>) als flache
     * Liste (<code>{A, B}</code>) zurück.
     * @param c Der Ausdruck
     * @param list Flache Liste, die den Ausdruck repräsentiert
     */
    protected void getFlattenedClassDescription(OntClass c, Set<CoraClassModel> list) {
        if(!c.isAnon()) {
            list.add(getFactory().wrapClass(c));
            return;
        }

        if(c.isRestriction()) {
            System.out.println("Is restriction");
        } else if(c.isIntersectionClass()) {
            IntersectionClass isc = c.asIntersectionClass();
            ExtendedIterator<? extends OntClass> iter = isc.listOperands();
            while(iter.hasNext()) {
                getFlattenedClassDescription(iter.next(), list);
            }
        } else if(c.isUnionClass()) {
            UnionClass unc = c.asUnionClass();
            ExtendedIterator<? extends OntClass> iter = unc.listOperands();
            while(iter.hasNext()) {
                getFlattenedClassDescription(iter.next(), list);
            }
        } else {
            //Ist etwas anderes...
            throw new NotImplementedException();
        }

    }
}
