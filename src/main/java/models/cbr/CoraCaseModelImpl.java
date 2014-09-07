package models.cbr;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import exceptions.MalformedOntologyException;
import models.ontology.CoraOntologyModelFactory;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by daniel on 20.08.14.
 */
public class CoraCaseModelImpl implements CoraCaseModel, ModelChangedListener {

    private CoraInstanceModel caseRoot;
    private CoraInstanceModel caseDescription;
    private CoraInstanceModel caseSolution = null;
    private CoraInstanceModel caseJustification = null;

    private OntModel caseModel;
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
    public CoraCaseModelImpl(String caseId, OntModel caseModel, CoraCaseBaseImpl caseBase)
            throws MalformedOntologyException, IOException {

        this.caseId = caseId;

        if(caseStructureMapping == null) {
            caseStructureMapping = new Properties();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(CASE_STRUCTURE_MAPPING_FILE);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
            caseStructureMapping.load(reader);
            reader.close();
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
    private void setupCaseModel(OntModel caseModel, CoraCaseBase caseBase, Properties structure)
            throws MalformedOntologyException {

        this.caseModel = caseModel;
        this.caseModel.register(this);

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
        checkOrCreateProperty(
                factory,
                caseRoot,
                caseDescription,
                domainNS + structure.getProperty("hasDescription", "hasCaseDescription"));

        checkOrCreateProperty(
                factory,
                caseRoot,
                caseSolution,
                domainNS + structure.getProperty("hasSolution", "hasCaseSolution"));

        checkOrCreateProperty(
                factory,
                caseRoot,
                caseJustification,
                domainNS + structure.getProperty("hasJustification", "hasCaseJustification"));
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


        OntClass clazz = caseModel.getOntClass(fullClazzName);
        if(clazz == null) {
            throw new MalformedOntologyException("Kann Klasse " + fullClazzName + " nicht finden.");
        }

        ExtendedIterator<? extends OntResource> iter = clazz.listInstances();
        while(iter.hasNext()) {
            OntResource resource = iter.next();
            if(resource.canAs(Individual.class)) {
                iter.close();
                return factory.wrapInstance(resource.asIndividual());
            }
        }

        Individual i = caseModel.createIndividual(fullIndvName, clazz);

        return factory.wrapInstance(i);
    }

    /**
     * Überprüft, ob <code>subject</code> ein Object-Property <code>fullPropertyName</code> hat, das <code>object</code>
     * als wert hat. Falls dies nicht der Fall ist, wird ein solches Property angelegt.
     * @param subject
     * @param object
     * @param fullPropertyName
     */
    private void checkOrCreateProperty(CoraOntologyModelFactory factory, CoraInstanceModel subject, CoraInstanceModel object, String fullPropertyName)
            throws MalformedOntologyException {
        ObjectProperty property = caseModel.getObjectProperty(fullPropertyName);
        if(property == null) {
            System.err.println("Property fehlt: " + fullPropertyName);
            throw new MalformedOntologyException();
        }

        if(!subject.getBaseObject().hasProperty(property, object.getBaseObject())) {
            CoraObjectPropertyModel coraProperty = factory.wrapObjectProperty(property);
            subject.createObjectRelation(coraProperty, object);
        }
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
        this.caseModel.getBaseModel().close();
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

    /*
     * Model change methods
     */

    @Override
    public void addedStatement(Statement statement) {
        System.out.println("Added statement: " + statement.getSubject() + " - " + statement.getPredicate() + " - " + statement.getObject());
    }

    @Override
    public void addedStatements(Statement[] statements) {

    }

    @Override
    public void addedStatements(List<Statement> statements) {

    }

    @Override
    public void addedStatements(StmtIterator stmtIterator) {

    }

    @Override
    public void addedStatements(Model model) {

    }

    @Override
    public void removedStatement(Statement statement) {
        System.out.println("Removed statement: " + statement.getSubject() + " - " + statement.getPredicate() + " - " + statement.getObject());
//        System.out.println("Vorhandene Statements:");
//        RDFNode node = statement.getObject();
//        if(node.isResource()) {
//            StmtIterator iter = node.asResource().listProperties();
//            while(iter.hasNext()) {
//                Statement s = iter.next();
//                System.out.println("Habe " + s.getSubject() + ", " + s.getPredicate() + ", " + s.getObject());
//            }
//        }
    }

    @Override
    public void removedStatements(Statement[] statements) {

    }

    @Override
    public void removedStatements(List<Statement> statements) {

    }

    @Override
    public void removedStatements(StmtIterator stmtIterator) {

    }

    @Override
    public void removedStatements(Model model) {

    }

    @Override
    public void notifyEvent(Model model, Object o) {

    }
}
