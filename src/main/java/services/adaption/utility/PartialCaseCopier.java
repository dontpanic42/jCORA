package services.adaption.utility;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import models.cbr.CoraCaseModelImpl;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraOntologyModelFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 11.10.14.
 *
 * Enthält hilfsfunktionen zum teilweisen Kopieren von Fällen.
 *
 * Die hier enthaltenen Funktionen arbeiten unter der Annahme, das in den lokalen
 * Fallontologien keine Klassen und Properties definiert werden und diese statt dessen
 * in der Domain-Ontologie gefunden werden können.
 *
 * TODO:
 * Instanzen, die mit dem zu kopierenden Fallteil nur mittelbar über eine Instanz der Domain-
 * Ontologie verbunden werden, werden <b>nicht</b> mit kopiert.
 */
public class PartialCaseCopier {

    /**
     * Kopiert die Fallbeschreibung des alten Falles <code>oldCase</code> in die Fallbeschreibung
     * eines neuen Falles <code>newCase</code>.
     * Wird als neuer Fall <code>null</code> übergeben, so wird ein neuer Fall angelegt.
     * @see models.cbr.CoraCaseModel#getCaseDescription()
     * @see models.cbr.CoraCaseBase#createTemporaryCase()
     * @param oldCase Ein alter Fall, der eine Fallbeschreibung enthält
     * @param newCase Ein neuer Fall oder <code>null</code>
     * @return Ein neuer Fall mit Fallbeschreibung
     * @throws Exception
     */
    public static CoraCaseModel copyCaseDescription(CoraCaseModel oldCase, CoraCaseModel newCase)
            throws Exception {

        if(newCase == null) {
            CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();
            newCase = caseBase.createTemporaryCase();
            System.out.println("PCC: Neuen Fall erzeugt.");
        }

        CoraInstanceModel oldInstance = oldCase.getCaseDescription();
        CoraInstanceModel newInstance = newCase.getCaseDescription();

        copyFromInstance(oldCase, newCase, oldInstance, newInstance);

        return newCase;
    }

    /**
     * Kopiert die Fallösung des alten Falles <code>oldCase</code> in die Fallösung
     * eines neuen Falles <code>newCase</code>.
     * Wird als neuer Fall <code>null</code> übergeben, so wird ein neuer Fall angelegt.
     * @see models.cbr.CoraCaseModel#getCaseSolution()
     * @see models.cbr.CoraCaseBase#createTemporaryCase()
     * @param oldCase Der alter Fall, der eine Fallösung enthält
     * @param newCase Ein neuer Fall oder <code>null</code>
     * @return Ein neuer Fall mit einer Fallösung
     * @throws Exception
     */
    public static CoraCaseModel copyCaseSolution(CoraCaseModel oldCase, CoraCaseModel newCase)
            throws Exception {

        if(newCase == null) {
            CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();
            newCase = caseBase.createTemporaryCase();
        }

        CoraInstanceModel oldInstance = oldCase.getCaseSolution();
        CoraInstanceModel newInstance = newCase.getCaseSolution();

        copyFromInstance(oldCase, newCase, oldInstance, newInstance);

        return newCase;
    }

    private static void copyFromInstance(CoraCaseModel oldCase, CoraCaseModel newCase, CoraInstanceModel oldInstance, CoraInstanceModel newInstance) {

        Map<CoraInstanceModel, CoraInstanceModel> visited = new HashMap<>();

        copyDataProperties(newCase, oldCase, oldInstance, newInstance);
        copyObjectProperties(newCase, oldCase, oldInstance, newInstance, visited);
    }

    private static CoraInstanceModel copyInstanceRec(CoraCaseModel oldCase,
                                            CoraCaseModel newCase,
                                            CoraInstanceModel instanceToCopy,
                                            Map<CoraInstanceModel, CoraInstanceModel> visited) {

        //Wenn die Instanz bereits bearbeitet wurde, oder die instanz Teil der Domain-Ontologie ist, breche ab.
        if(visited.containsKey(instanceToCopy)) {
            return visited.get(instanceToCopy);
        }

        if(!isPartOfCaseOntology(instanceToCopy, oldCase)) {
            return instanceToCopy;
        }

        CoraInstanceModel copiedInstance = copyInstance(oldCase, newCase, instanceToCopy);
        if(copiedInstance == null) {
            return null;
        }

        visited.put(instanceToCopy, copiedInstance);

        //Kopiere die Data-Properties
        copyDataProperties(newCase, oldCase, instanceToCopy, copiedInstance);
        copyObjectProperties(newCase, oldCase, instanceToCopy, copiedInstance, visited);

        return copiedInstance;
    }

    /**
     * Kopiert alle DataProperties einer Instanz.
     * Annhame: Alle DataProperties sind in der Domain-Ontologie definiert, müssen also nicht kopiert werden
     * @param newCase
     * @param oldCase
     * @param instanceToCopy
     * @param copiedInstance
     */
    private static void copyDataProperties(CoraCaseModel newCase,
                                           CoraCaseModel oldCase,
                                           CoraInstanceModel instanceToCopy,
                                           CoraInstanceModel copiedInstance) {
        OntModel newOntModel = ((CoraCaseModelImpl) newCase).getModel();
        OntModel oldOntModel = ((CoraCaseModelImpl) oldCase).getModel();
        Resource subject = copiedInstance.getBaseObject();

        StmtIterator iter = oldOntModel.listStatements(instanceToCopy.getBaseObject(), null, (RDFNode) null);
        while(iter.hasNext()) {
            Statement s = iter.next();
            if(s.getPredicate().canAs(DatatypeProperty.class)) {
                Literal oldLiteral = s.getObject().asLiteral();
                //Literal newLiteral = newOntModel.createLiteral(oldLiteral.getLexicalForm(), true);

                Property p = s.getPredicate();
                Property newProperty = newOntModel.getProperty(p.getNameSpace(), p.getLocalName());
                if(newProperty == null) {
                    System.err.println("PCC: Property nicht gefunden: " + p.getLocalName());
                    continue;
                }

                Statement newStatement = newOntModel.createStatement(subject, newProperty, oldLiteral);
                newOntModel.add(newStatement);
            }
        }
    }

    /**
     * Kopiert die ObjectProperties einer Instanz rekursiv, d.h. die Zielinstanzen (Objekte) werden ebenfalls
     * kopiert.
     * Annhame: Alle ObjectProperties sind in der Domain-Ontologie definiert, müssen also nicht kopiert werden
     * @param newCase
     * @param oldCase
     * @param instanceToCopy
     * @param copiedInstance
     * @param visited
     */
    private static void copyObjectProperties(CoraCaseModel newCase,
                                             CoraCaseModel oldCase,
                                             CoraInstanceModel instanceToCopy,
                                             CoraInstanceModel copiedInstance,
                                             Map<CoraInstanceModel, CoraInstanceModel> visited) {
        OntModel newOntModel = ((CoraCaseModelImpl) newCase).getModel();
        OntModel oldOntModel = ((CoraCaseModelImpl) oldCase).getModel();
        Resource subject = copiedInstance.getBaseObject();
        CoraOntologyModelFactory oldFactory = ((CoraCaseModelImpl) oldCase).getFactory();

        StmtIterator iter = oldOntModel.listStatements(instanceToCopy.getBaseObject(), null, (RDFNode) null);
        while(iter.hasNext()) {
            Statement s = iter.next();
            if(s.getPredicate().canAs(ObjectProperty.class)) {
                CoraInstanceModel oldObject = oldFactory.wrapInstance(s.getObject().as(Individual.class));

                //Wenn das ObjectProperty die selbe Instanz als subjekt als auch als objekt hat,
                //benutze nicht die rekursive methode (sonst könnte es zu endlosschleifen kommen)
                CoraInstanceModel object;
                if(oldObject.equals(instanceToCopy)) {
                    object = copiedInstance;
                } else {
                    object = copyInstanceRec(oldCase, newCase, oldObject, visited);
                }

                Property p = s.getPredicate();
                Property newProperty = newOntModel.getProperty(p.getNameSpace(), p.getLocalName());
                if(newProperty == null) {
                    System.err.println("PCC: Property nicht gefunden: " + p.getLocalName());
                    continue;
                }

                Statement newStatement = newOntModel.createStatement(subject, newProperty, object.getBaseObject());
                newOntModel.add(newStatement);
            }
        }
    }

    /**
     * Kopiert eine instanz aus einem Fall in einen neuen Fall.
     * Annahme: Die Elternklassen der Instanz liegen allesamt innerhalb der Domain-Ontologie, können also in beiden
     * Modellen (alt und neu) verwendet werden. Gilt diese Annahme nicht, müssten die Klassen ebenfalls kopiert werden-
     * dies ist hier jedoch nicht implementiert.
     * @param oldCase Der Ausgangsfall
     * @param newCase Der Zielfall
     * @param instanceToCopy Die Instanz, die kopiert werden soll
     * @return Die neue Instanz (Kopie), oder <code>null</code>, wenn die zu kopierende Instanz keine Elternklasse besitzt.
     */
    private static CoraInstanceModel copyInstance(CoraCaseModel oldCase, CoraCaseModel newCase, CoraInstanceModel instanceToCopy) {
        //Suche die Elternklassen der zu kopierenden Instanz
        ExtendedIterator<OntClass> iter = instanceToCopy.getBaseObject().listOntClasses(true);
        //Wenn mindestens eine Elternklasse existiert...
        if(iter.hasNext()) {
            CoraCaseModelImpl newCaseImpl = (CoraCaseModelImpl) newCase;
            OntModel ontModel = newCaseImpl.getModel();

            //Der neue Instanzname = ns des neuen Falles + name der alten Instanz
            String instanceName = newCase.getCaseDescription().getNs() + instanceToCopy.toString();

            //TODO: Überprüfen ob eine Instanz dieses Namens schon besteht. In dem Fall müsste der Name der neuen Instanz geändert werden
            //i.g. Instanz2 oder so. ???
            //Erzeuge die Instanz auf dem neuen Modell
            Individual i = ontModel.createIndividual(instanceName, iter.next());

            //Füge die restlichen Elternklassen hinzu.
            while(iter.hasNext()) {
                i.addOntClass(iter.next());
            }

            //Gib das ganze als CoraInstance zurück
            return newCaseImpl.getFactory().wrapInstance(i);
        }

        return null;
    }

    private static boolean isPartOfCaseOntology(CoraInstanceModel instance, CoraCaseModel originalCase) {
        String instanceNS = instance.getNs();
        String caseNS = originalCase.getCaseDescription().getNs();

        return instanceNS.equals(caseNS);
    }
}
