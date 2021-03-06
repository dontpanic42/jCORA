package models.ontology;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import mainapp.MainApplication;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Set;

/**
 * Diese Klasse repräsentiert ein Element aus der Ontologie
 */
public class CoraOntologyModel<T extends OntResource> {

    private T baseObject;
    private OntModel model;
    private CoraOntologyModelFactory factory;

    /**
     * Getter für das Jena Basis-Objekt
     * @return Das Jena-Objekt, auf dem dieses Objekt basiert
     */
    public T getBaseObject() {
        return baseObject;
    }

    /**
     * Setter für das Jena Basis-Objekt
     * @param baseObject Das Jena-Objekt, auf dem dieses Objekt basiert
     */
    public void setBaseObject(T baseObject) {
        this.baseObject = baseObject;
    }

    /**
     * Getter für das Jena Ontologie-Modell, in dem dieses Objekt zu finden ist
     * @return Das Jena Ontologie-Modell
     */
    public OntModel getModel() {
        return model;
    }

    /**
     * Setter für das Jena Ontologie-Modell, in dem dieses Objekt zu finden ist.
     * @param model Das Jena Ontologie-Modell
     */
    public void setModel(OntModel model) {
        this.model = model;
    }

    /**
     * Gibt dieses Objekt als <code>String</code> zurück
     * @return String-Repräsentation dieses Objekts
     */
    @Override
    public String toString() {
        return this.baseObject.getLocalName();
    }

    /**
     * Gibt den Namespace dieses Objekts zurück
     * @return Der Namespace
     */
    public String getNs() {
        //return this.model.getNsPrefixURI("");
        return this.baseObject.getNameSpace();
    }

    /**
     * Getter für die Model-Factory, die dieses Objekt erzeugt hat
     * @return Die Factory
     */
    public CoraOntologyModelFactory getFactory() {
        return factory;
    }

    /**
     * Setter für die Model-Factory.
     * @param factory Die Factory
     */
    public void setFactory(CoraOntologyModelFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoraOntologyModel)) return false;

        CoraOntologyModel that = (CoraOntologyModel) o;

        return baseObject.equals(that.baseObject);

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

    /**
     * Gibt den Anzeigenamen des Objekts zurück, d.h. ein definiertes Label in der aktuellen Sprache, oder,
     * falls ein solches nicht existiert, den <code>localName</code> des objekts.
     * @param lang Die Sprache (en, de, fr,...)
     * @return Der Anzeigename oder der <code>localName</code>
     */
    public String getDisplayName(String lang) {
        ExtendedIterator<RDFNode> labels = getBaseObject().listLabels(lang);

        if(labels.hasNext()) {
            final RDFNode node = labels.next();
            final Literal literal = node.asLiteral();
            return literal.getString();
        }

        /* Der lokale Name ist null, wenn die Resource anonym ist */
        if(getBaseObject().getLocalName() == null) {
            return "(anonym)";
        }

        return getBaseObject().getLocalName();
    }

    /**
     * Gibt zurück, ob dieses Objekt ein Teil der Domänenontologie ist. Wenn dieses Objekt keinen
     * Namensraum besitzt (also z.B. ein BNode ist), wird davon ausgegangen, das dieser zur Fallontologie
     * gehört.
     * @return <code>true</code>, wenn das Objekt zur Domänenontologie gehört
     */
    public boolean isPartOfDomainOntology() {
        String instanceNs = getNs();
        String domainNs = MainApplication.getInstance().getCaseBase().getDomainNs();

        /* instanceNs kann null werden, wenn die instanz anonym ist... */
        return (instanceNs != null && instanceNs.equals(domainNs));
    }
}
