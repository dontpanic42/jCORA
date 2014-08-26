package models.cbr;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import exceptions.MalformedOntologyException;
import factories.ontology.CoraOntologyModelFactory;
import models.ontology.CoraInstanceModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by daniel on 20.08.14.
 */
public class CoraCaseModelImpl implements CoraCaseModel {

    private CoraInstanceModel caseRoot;
    private CoraInstanceModel caseDescription;
    private CoraInstanceModel caseSolution = null;
    private CoraInstanceModel caseJustification = null;

    private OntModel caseModel;
    private Dataset dataset;
    private String caseId;

    private Set<CaseChangeHandler> onChangeHandlers = new HashSet<>();

    private static Properties caseStructureMapping = null;
    private static final String CASE_STRUCTURE_MAPPING_FILE = "config/casestructure.properties";

    /**
     * Erzeugt einen Fall mit der standard-Fallstruktur
     * @param caseModel Das Fall-Modell
     * @param caseBase  Die zugehörige Case-Base
     * @throws MalformedOntologyException Wenn die Ontologie nicht mit der vorgegebenen Fallstruktur übereinstimmt
     * @throws IOException Wenn die Fallstruktur-Konfiguration nicht gelesen werden kann
     */
    public CoraCaseModelImpl(String caseId, OntModel caseModel, CoraCaseBaseImpl caseBase, Dataset dataset)
            throws MalformedOntologyException, IOException {

        this.caseId = caseId;

        if(caseStructureMapping == null) {
            caseStructureMapping = new Properties();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(CASE_STRUCTURE_MAPPING_FILE);
            caseStructureMapping.load(is);
            is.close();
        }

        this.dataset = dataset;
        setupCaseModel(caseModel, caseBase, caseStructureMapping, dataset);
    }

    /**
     * Erzeugt einen Fall
     * @param caseModel Das Fall-Modell
     * @param caseBase  Die zugehörige Case-Base
     * @param structure Die Fallstruktur
     * @throws MalformedOntologyException Wenn die Ontologie nicht mit der vorgegebenen Fallstruktur übereinstimmt
     */
    public CoraCaseModelImpl(OntModel caseModel, CoraCaseBaseImpl caseBase, Properties structure, Dataset dataset)
            throws MalformedOntologyException  {

        this.caseId = caseId;

        this.dataset = dataset;
        setupCaseModel(caseModel, caseBase, structure, dataset);
    }

    public OntModel getModel() {
        return caseModel;
    }

    /**
     * Initialisiert den Fall.
     * @param caseModel Das Case-Model
     * @param caseBase Die Fallbasis
     * @param structure Die Fallstruktur-Konfiguration
     * @throws MalformedOntologyException Wenn die Ontologie nicht mit der vorgegebenen Fallstruktur übereinstimmt
     */
    private void setupCaseModel(OntModel caseModel, CoraCaseBase caseBase, Properties structure, Dataset dataset)
            throws MalformedOntologyException {

        this.caseModel = caseModel;

        CoraOntologyModelFactory factory = new CoraOntologyModelFactory(caseModel, this);
        createCaseStructure(factory, ((CoraCaseBaseImpl) caseBase).getDomainModel(), caseModel, structure);
    }

    /**
     * Überprüft, ob die vorgegebene Fallstruktur vorhanden ist und erzeugt diese, falls nötig.
     * @param factory Die Modell-Factory
     * @param domainModel Die Domain-Ontologie
     * @param caseModel Die Fall-Ontollgie
     * @param structure Die Fallstruktur
     * @throws MalformedOntologyException Wenn die Ontologie nicht mit der vorgegebenen Fallstruktur übereinstimmt
     */
    private void createCaseStructure(CoraOntologyModelFactory factory, OntModel domainModel, OntModel caseModel, Properties structure)
            throws MalformedOntologyException {
        String domainNS = domainModel.getNsPrefixURI("");
        String localNS = caseModel.getNsPrefixURI("");

        caseRoot = getOrCreateIndividual(
                factory,
                localNS + structure.getProperty("rootIndv", "I_Case"),
                domainNS + structure.getProperty("rootClass", "Case"),
                caseModel);

        caseDescription = getOrCreateIndividual(
                factory,
                localNS + structure.getProperty("descriptionIndv", "I_CaseDescription"),
                domainNS + structure.getProperty("descriptionClass", "CaseDescription"),
                caseModel);

        caseSolution = getOrCreateIndividual(
                factory,
                localNS + structure.getProperty("solutionIndv", "I_CaseSolution"),
                domainNS + structure.getProperty("solutionClass", "CaseSolution"),
                caseModel);

        caseJustification = getOrCreateIndividual(
                factory,
                localNS + structure.getProperty("justificationIndv", "I_CaseJustification"),
                domainNS + structure.getProperty("justificationClass", "CaseJustification"),
                caseModel);

        //TODO: Create properties
    }

    /**
     * Überprüft, ob eine Instanz mit Namen <code>fulIndvName</code> der Klasse <code>fullClazzName</code> vorhanden
     * ist und erzeugt diese, falls nötig.
     * @param factory Die Modell-Factory
     * @param fullIndvName Der Name der Instanz inkl. Prefix
     * @param fullClazzName Der Name der Klasse inkl. Prefix
     * @param caseModel Das Fall-Modell
     * @return Die Instanz als <code>CoraInstanceModel</code>
     * @throws MalformedOntologyException Wenn die Klasse nicht in der Ontologie vorhanden ist
     */
    private CoraInstanceModel getOrCreateIndividual(CoraOntologyModelFactory factory, String fullIndvName, String fullClazzName, OntModel caseModel)
            throws MalformedOntologyException {

        //Vorher
//        Individual i = caseModel.getIndividual(fullIndvName);
//        if(i == null) {
//            OntClass clazz = caseModel.getOntClass(fullClazzName);
//            if(clazz == null) {
//                System.err.println("Kann Klasse nicht finden: " + fullClazzName);
//                throw new MalformedOntologyException();
//            }
//
//            i = caseModel.createIndividual(fullIndvName, clazz);
//        }


        if(dataset != null) {
            dataset.begin(ReadWrite.READ);
        }

        OntClass clazz = caseModel.getOntClass(fullClazzName);
        ExtendedIterator<? extends OntResource> iter = clazz.listInstances();
        while(iter.hasNext()) {
            OntResource resource = iter.next();
            if(resource.canAs(Individual.class)) {
                iter.close();

                if(dataset != null) {
                    dataset.end();
                }

                CoraInstanceModel imod = factory.wrapInstance(resource.asIndividual());
                return imod;
            }
        }

        if(dataset != null) {
            dataset.end();
            dataset.begin(ReadWrite.WRITE);
        }

        Individual i = caseModel.createIndividual(fullIndvName, clazz);

        if(dataset != null) {
            dataset.commit();
            dataset.end();
        }

        return factory.wrapInstance(i);
    }

    /**
     * Gibt die Instanz zurück, die die Wurzel dieses Falles
     * bildet.
     * @return Die Wurzel des Falles
     */
    @Override
    public CoraInstanceModel getCaseRoot() {
        return caseRoot;
    }

    /**
     * Gibt die Instanz zurück, die die Wurzel der Beschreibung dieses Falles
     * bildet.
     * @return Die Wurzel der Fallbeschreibung
     */
    @Override
    public CoraInstanceModel getCaseDescription() {
        return caseDescription;
    }

    /**
     * Gibt die Instanz zurück, die die Wurzel der Lösung dieses Falles bildet.
     * @return Die Wurzel der Falllösung
     */
    @Override
    public CoraInstanceModel getCaseSolution() {
        return caseSolution;
    }

    /**
     * Gibt die Instanz zurück, die die Wurzel der Bewertung dieses Falles bildet.
     * @return
     */
    @Override
    public CoraInstanceModel getCaseJustification() {
        return caseJustification;
    }

    /**
     * Schließt die Verwendung dieses Modells ab.
     */
    @Override
    public void close() {
        this.caseModel.close();
    }

    @Override
    public Set<CaseChangeHandler> getOnChangeHandlers() {
        return onChangeHandlers;
    }

    @Override
    public void addOnChangeHandler(CaseChangeHandler handler) {
        onChangeHandlers.add(handler);
    }

    @Override
    public void removeOnChangeHandler(CaseChangeHandler handler) {
        onChangeHandlers.remove(handler);
    }

    @Override
    public String getCaseId() {
        return caseId;
    }
}
