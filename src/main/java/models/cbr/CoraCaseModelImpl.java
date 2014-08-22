package models.cbr;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import exceptions.MalformedOntologyException;
import factories.ontology.CoraOntologyModelFactory;
import models.ontology.CoraInstanceModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by daniel on 20.08.14.
 */
public class CoraCaseModelImpl implements CoraCaseModel {

    private CoraInstanceModel caseRoot;
    private CoraInstanceModel caseDescription;
    private CoraInstanceModel caseSolution = null;
    private CoraInstanceModel caseJustification = null;

    private OntModel caseModel;

    private static Properties caseStructureMapping = null;
    private static final String CASE_STRUCTURE_MAPPING_FILE = "src/main/config/casestructure.properties";

    /**
     * Erzeugt einen Fall mit der standard-Fallstruktur
     * @param caseModel Das Fall-Modell
     * @param caseBase  Die zugehörige Case-Base
     * @throws MalformedOntologyException Wenn die Ontologie nicht mit der vorgegebenen Fallstruktur übereinstimmt
     * @throws IOException Wenn die Fallstruktur-Konfiguration nicht gelesen werden kann
     */
    public CoraCaseModelImpl(OntModel caseModel, CoraCaseBaseImpl caseBase)
            throws MalformedOntologyException, IOException {

        if(caseStructureMapping == null) {
            caseStructureMapping = new Properties();
            InputStream is = new FileInputStream(CASE_STRUCTURE_MAPPING_FILE);
            caseStructureMapping.load(is);
            is.close();
        }

        setupCaseModel(caseModel, caseBase, caseStructureMapping);
    }

    /**
     * Erzeugt einen Fall
     * @param caseModel Das Fall-Modell
     * @param caseBase  Die zugehörige Case-Base
     * @param structure Die Fallstruktur
     * @throws MalformedOntologyException Wenn die Ontologie nicht mit der vorgegebenen Fallstruktur übereinstimmt
     */
    public CoraCaseModelImpl(OntModel caseModel, CoraCaseBaseImpl caseBase, Properties structure)
            throws MalformedOntologyException  {

        setupCaseModel(caseModel, caseBase, structure);
    }

    /**
     * Initialisiert den Fall.
     * @param caseModel Das Case-Model
     * @param caseBase Die Fallbasis
     * @param structure Die Fallstruktur-Konfiguration
     * @throws MalformedOntologyException Wenn die Ontologie nicht mit der vorgegebenen Fallstruktur übereinstimmt
     */
    private void setupCaseModel(OntModel caseModel, CoraCaseBase caseBase, Properties structure)
            throws MalformedOntologyException {

        this.caseModel = caseModel;

        CoraOntologyModelFactory factory = new CoraOntologyModelFactory(caseModel);
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

        Individual i = caseModel.getIndividual(fullIndvName);
        if(i == null) {
            OntClass clazz = caseModel.getOntClass(fullClazzName);
            if(clazz == null) {
                System.err.println("Kann Klasse nicht finden: " + fullClazzName);
                throw new MalformedOntologyException();
            }

            i = caseModel.createIndividual(fullIndvName, clazz);
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
}
