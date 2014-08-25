package factories.ontology;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.query.ReadWrite;
import exceptions.ResourceAlreadyExistsException;
import models.cbr.CoraCaseModel;
import models.cbr.CoraCaseModelImpl;
import models.ontology.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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

//        tryLock(ReadWrite.WRITE);
//
//        //Versuchen das individual mit dem Namen zu laden...
//        Individual i = model.getIndividual(model.getNsPrefixURI("") + name);
//        if(i != null) {
//            tryEndLock();
//
//            throw new ResourceAlreadyExistsException();
//        }
//
//        //Erzeuge die Instanz
//        i = model.createIndividual(model.getNsPrefixURI("") + name,
//                fromClass.getBaseObject());
//
//        CoraInstanceModel instance = wrapInstance(i);
//
//        //Löse das Datenbank-Lock
//        tryCommit();
//        tryEndLock();
//
//        //Triggere das Case-Change event
//        if(caseModel != null) {
//            for(CoraCaseModel.CaseChangeHandler handler : caseModel.getOnChangeHandlers()) {
//                handler.onAddInstance(instance);
//            }
//        }
//
//        return instance;
    }

    public void tryLock(ReadWrite type) {
        if(caseModel instanceof CoraCaseModelImpl) {
            ((CoraCaseModelImpl) caseModel).tryLock(type);
        }
    }

    public void tryCommit() {
        if(caseModel instanceof CoraCaseModelImpl) {
            ((CoraCaseModelImpl) caseModel).tryCommit();
        }
    }

    public void tryEndLock() {
        if(caseModel instanceof CoraCaseModelImpl) {
            ((CoraCaseModelImpl) caseModel).tryEndLock();
        }
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

        throw new NotImplementedException();
    }
}
