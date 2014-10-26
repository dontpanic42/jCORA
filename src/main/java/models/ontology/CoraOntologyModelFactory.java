package models.ontology;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Statement;
import exceptions.ResourceAlreadyExistsException;
import models.cbr.CoraCaseModel;
import models.ontology.assertions.DataPropertyAssertion;

import java.util.Map;

/**
 * Created by daniel on 20.08.14.
 */
public class CoraOntologyModelFactory {

    private OntModel model;
    private CoraCaseModel caseModel = null;

    public CoraOntologyModelFactory(OntModel model, CoraCaseModel caseModel) {
        this.model = model;
        this.caseModel = caseModel;
    }

    public CoraCaseModel getCase() {
        return caseModel;
    }

    /**
     * Erzeugt eine neue Instanz vom typ <code>fromClass</code> mit dem Namen
     * <code>(lokaler NS + )name</code>
     * @param fromClass typ der Instanz
     * @param name Name der Instanz ohne Namespace
     * @return neue Instanz
     * @throws ResourceAlreadyExistsException wenn eine Instanz mit dem Namen bereits existiert
     */
    public CoraInstanceModel createInstance(CoraClassModel fromClass, String name)
            throws ResourceAlreadyExistsException {
        return createInstance(fromClass, name, null);
    }

    /**
     * Erzeugt eine neue Instanz vom typ <code>fromClass</code> mit dem Namen
     * <code>(lokaler NS + )name</code>
     * @param fromClass typ der Instanz
     * @param name Name der Instanz ohne Namespace
     * @param labels Die Anzeigenamen (Labels) der Instanz im Format (Language, String)
     * @return neue Instanz
     * @throws ResourceAlreadyExistsException wenn eine Instanz mit dem Namen bereits existiert
     */
    public CoraInstanceModel createInstance(CoraClassModel fromClass, String name, Map<String, String> labels)
            throws ResourceAlreadyExistsException {

        System.out.println("Model NS: " + model.getNsPrefixURI(""));

        //Versuchen das individual mit dem Namen zu laden...
        Individual i = model.getIndividual(model.getNsPrefixURI("") + name);
        if(i != null) {
            throw new ResourceAlreadyExistsException();
        }

        System.out.println("Erzeuge instanz mit namen: " + model.getNsPrefixURI("") + name);

        //Erzeuge die Instanz
        i = model.createIndividual(model.getNsPrefixURI("") + name,
                fromClass.getBaseObject());

        if(labels != null) {
            for(Map.Entry<String, String> e : labels.entrySet()) {
                i.addLabel(e.getValue(), e.getKey());
            }
        }

        CoraInstanceModel instance = wrapInstance(i);

        //Triggere das Case-Change event
        if(caseModel != null) {
            for(CoraCaseModel.CaseChangeHandler handler : caseModel.getOnChangeHandlers()) {
                handler.onAddInstance(instance);
            }
        }

        return instance;
    }

    public CoraClassModel wrapClass(OntClass clazz) {
        CoraClassModel model = new CoraClassModel();
        model.setBaseObject(clazz);
        model.setModel(this.model);
        model.setFactory(this);

        return model;
    }

    public CoraInstanceModel wrapInstance(Individual indv) {
        CoraInstanceModel model = new CoraInstanceModel();
        model.setBaseObject(indv);
        model.setModel(this.model);
        model.setFactory(this);

        return model;
    }

    public CoraObjectPropertyModel wrapObjectProperty(ObjectProperty prop) {
        CoraObjectPropertyModel model = new CoraObjectPropertyModel();
        model.setBaseObject(prop);
        model.setModel(this.model);
        model.setFactory(this);

        return model;
    }

    public CoraDataPropertyModel wrapDataProperty(DatatypeProperty prop) {
        CoraDataPropertyModel model = new CoraDataPropertyModel();
        model.setBaseObject(prop);
        model.setModel(this.model);
        model.setFactory(this);

        return model;
    }

    public CoraPropertyModel<?> wrapProperty(OntProperty prop) {
        if(prop.canAs(DatatypeProperty.class)) {
            return wrapDataProperty(prop.as(DatatypeProperty.class));
        } else if(prop.canAs(ObjectProperty.class)) {
            return wrapObjectProperty(prop.as(ObjectProperty.class));
        }

        return null;
    }

    public DataPropertyAssertion wrapDataPropertyStatement(Statement s) {
        DataPropertyAssertion assertion = new DataPropertyAssertion(s);
        assertion.setFactory(this);
        return assertion;
    }
}
