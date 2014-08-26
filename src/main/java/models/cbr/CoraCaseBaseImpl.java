package models.cbr;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelGetter;
import com.hp.hpl.jena.rdf.model.ModelReader;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.naming.ConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by daniel on 22.08.14.
 */
public class CoraCaseBaseImpl implements CoraCaseBase {

    /**
     * Pfad zur Standard-Konfiguration
     */
    private static final String CASE_BASE_PROPERTIES_FILE = "config/casebase.properties";

    private LocalModelGetter localModelGetter;
    private OntModel domainModel;
    private Dataset dataset;
    private OntDocumentManager documentManager;

    private Properties caseBaseProperties;

    private List<CaseBaseChangeHandler> caseBaseChangeHandlers = new ArrayList<CaseBaseChangeHandler>();

    /**
     * Erzeugt ein neues <code>CoraCaseBaseImpl</code>-Objekt mit der Standard-Konfiguration
     * @throws ConfigurationException Wenn die Konfiguration fehlerhaft ist
     * @throws FileNotFoundException Wenn die Domain-Ontologie oder die Standardkonfiguration nicht gefunden wird
     */
    public CoraCaseBaseImpl() throws ConfigurationException, FileNotFoundException {
        Properties defaultProperties = new Properties();

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(CASE_BASE_PROPERTIES_FILE);
            defaultProperties.load(is);
            is.close();
        } catch (IOException e) {
            throw new FileNotFoundException("Keine CaseBase-Konfiguration gefunden");
        }

        setupCaseBase(defaultProperties);
    }

    /**
     * Erzeugt ein neues <code>CoraCaseBaseImpl</code>-Objekt
     * @param properties Die Case-Base Konfiguration
     * @throws ConfigurationException Wenn die Konfiguration fehlerhaft ist
     * @throws FileNotFoundException Wenn die Domain-Ontologie nicht gefunden wird
     */
    public CoraCaseBaseImpl(Properties properties) throws ConfigurationException, FileNotFoundException {
        setupCaseBase(properties);
    }

    /**
     * Initialisiert die Case-Base
     * @param properties Die Case-Base Konfiguration
     * @throws ConfigurationException
     * @throws FileNotFoundException
     */
    private void setupCaseBase(Properties properties) throws ConfigurationException, FileNotFoundException {
        caseBaseProperties = properties;

        domainModel = loadDomainModel();

        localModelGetter = new LocalModelGetter();
        localModelGetter.addModel(domainModel);

        dataset = createTDBDataset();
        documentManager = new OntDocumentManager();
    }

    /**
     * Gibt einen Fall mit dem Namen <code>name</code> zurück. Existiert kein Fall mit diesem Namen, wird ein leerer
     * Fall mit diesem Namen erzeugt.
     * @param name Der Name (id) der Ontologie
     * @return Die Ontologie als Cbr-Fall
     * @throws Throwable
     */
    @Override
    public CoraCaseModel loadCase(String name) throws Throwable {
        dataset.begin(ReadWrite.READ);

        Model tdbModel = dataset.getNamedModel(name);
        Model m = ModelFactory.createDefaultModel();
        m.add(tdbModel);
        //TODO: Prefixes?
        m.setNsPrefix("", tdbModel.getNsPrefixURI(""));

        dataset.end();

        OntModelSpec modelSpec = new OntModelSpec(PelletReasonerFactory.THE_SPEC);
        modelSpec.setImportModelGetter(localModelGetter);
        modelSpec.setDocumentManager(documentManager);
        documentManager.setProcessImports(false);


        OntModel model = ModelFactory.createOntologyModel(modelSpec, m);

        documentManager.setProcessImports(true);
        documentManager.loadImport(model, domainModel.getNsPrefixURI(""));

        CoraCaseModelImpl caseModel = new CoraCaseModelImpl(name, model, this);
        return caseModel;
    }

    @Override
    public void importCase(String asId, String fileFormat, File file) throws IOException {
        dataset.begin(ReadWrite.WRITE);

        //TODO: Strip existing domain-ontology imports from model!
        Model m = dataset.getNamedModel(asId);

        try {
            InputStream is = new FileInputStream(file);
            m.read(is, fileFormat);
            dataset.commit();

            m.close();
            dataset.end();

            System.out.println("Imported " + asId + " file: " + file.getAbsolutePath() + " format: " + fileFormat);
            for(CaseBaseChangeHandler h : caseBaseChangeHandlers) {
                h.onAddCase(asId);
            }
        } catch (FileNotFoundException e) {
            dataset.end();
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Erzeugt einen Fall, der nicht in der Datenbank gesichert ist, sonst jedoch
     * alle eigenschaften eines normalen Falls aufweist. Soll zur erstellung der
     * Falleingabe dienen. Ein so erzeugte Fall ist nicht in der Fallbasis vorhanden.
     * @return Ein nicht in der Datenbank gesicherten Fall
     * @throws Throwable
     */
    @Override
    public CoraCaseModel createTemporaryCase() throws Throwable {
        OntModelSpec modelSpec = new OntModelSpec(PelletReasonerFactory.THE_SPEC);
        modelSpec.setImportModelGetter(localModelGetter);
        modelSpec.setDocumentManager(documentManager);

        OntModel model = ModelFactory.createOntologyModel(modelSpec);
        documentManager.loadImport(model, domainModel.getNsPrefixURI(""));

        CoraCaseModelImpl caseModel = new CoraCaseModelImpl(null, model, this);
        return caseModel;
    }

    /**
     * Gibt zurück, ob ein Fall mit dem Namen <code>name</code> in der Case-Base existiert.
     * @param name Name des zu suchenden Falles
     * @return <code>true</code>, wenn der Fall existiert
     */
    @Override
    public boolean caseExists(String name) {
        return dataset.containsNamedModel(name);
    }

    /**
     * Gibt die Anzahl der in dieser Fallbasis gespeicherten Fälle zurück.
     * @return Anzahl der in dieser Fallbasis gespeicherten Fälle
     */
    @Override
    public int size() {
        Iterator<String> iter = dataset.listNames();
        int i = 0;
        while(iter.hasNext()) {
            i++;
            iter.next();
        }

        return i;
    }

    /**
     * Gibt einen Iterator zurück, der es ermöglicht, über alle in der Fallbasis vorhandenen Fälle zu iterieren.
     * @return Iterator über alle Fälle
     */
    @Override
    public Iterator<CoraCaseModel> getCases() {
        return new CaseBaseIterator(dataset.listNames(), this);
    }

    /**
     * Gibt einen Iterator zurück, der über die IDs aller Fälle iteriert.
     * @return Iterator über alle Fall-IDs
     */
    @Override
    public Iterator<String> listCaseIDs() {
        return dataset.listNames();
    }

    /**
     * Läd die in der Konfiguration (<code>casBaseProperties</code>) hinterlegte Domain-Ontologie
     * @return
     * @throws ConfigurationException
     * @throws FileNotFoundException
     */
    private OntModel loadDomainModel()
            throws ConfigurationException, FileNotFoundException {

        String domainModelFile = caseBaseProperties.getProperty("domainModelFile");

        if(domainModelFile == null) {
            throw new ConfigurationException();
        }

        if(!(new File(domainModelFile).exists())) {
            throw new FileNotFoundException();
        }

        //OntModel domainModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
        OntModel domainModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        domainModel.read(FileManager.get().open(domainModelFile), "RDF/XML");

        if(caseBaseProperties.getProperty("domainNsOverride", null) != null) {
            domainModel.setNsPrefix("", caseBaseProperties.getProperty("domainNsOverride"));
        }

        String ns = domainModel.getNsPrefixURI("");

        System.out.println(ns);

        return domainModel;
    }

    @Override
    public String getDomainNs() {
        return domainModel.getNsPrefixURI("");
    }

    /**
     * (Für Tests: Gibt die Domain-Ontologie zurück.)
     * @return
     */
    public OntModel getDomainModel() {
        return domainModel;
    }

    /**
     * Schließt die CaseBase und schreibt jeweilige Änderungen zurück in die Datenbank
     */
    @Override
    public void close() {
        if(dataset != null) {
            dataset.close();
        }
    }

    public void save(CoraCaseModel caseModel) {
        String id = caseModel.getCaseId();
        if(id == null) {
            System.err.println("No case Id!");
            return;
        }

        if(!(caseModel instanceof CoraCaseModelImpl)) {
            System.err.println("Nicht instanceof CoraCaseModeImpl");
            return;
        }

        OntModel original = ((CoraCaseModelImpl) caseModel).getModel();
        Model toSave = original.getBaseModel();

        dataset.begin(ReadWrite.WRITE);

        dataset.replaceNamedModel(id, toSave);

        dataset.commit();
        dataset.end();

        System.out.println("Saved");
    }

    /**
     * Initialisiert die TDB-Datenbank, wie in der Konfiguration (<code>caseBaseProperties</code>)
     * spezifziert
     * @return Das Dataset, das die Datenbank repräsentiert
     * @throws ConfigurationException Wenn die Konfiguration fehlerhaft ist
     */
    private Dataset createTDBDataset()
            throws ConfigurationException {

        String tdbPath = caseBaseProperties.getProperty("tdbCaseBase");
        if(tdbPath == null) {
            throw new ConfigurationException("TDB-CaseBase nicht spezifiziert");
        }

        new File(tdbPath).mkdirs();

        Dataset dataset = TDBFactory.createDataset(tdbPath);
        return dataset;
    }

    @Override
    public void addCaseBaseChangeHandler(CaseBaseChangeHandler handler) {
        caseBaseChangeHandlers.add(handler);
    }

    @Override
    public void removeCaseBaseChangeHandler(CaseBaseChangeHandler handler) {
        caseBaseChangeHandlers.remove(handler);
    }

    /**
     * Implementiert einen Iterator über alle Fälle einer Fallbasis
     */
    private class CaseBaseIterator implements Iterator<CoraCaseModel> {
        private Iterator<String> nameIterator;
        private CoraCaseBase caseBase;

        /**
         * Konstruktor
         * @param nameIterator Der Iterator aus <code>dataset.listNames</code>.
         * @param caseBase Die zugehörige Fallbasis
         */
        public CaseBaseIterator(Iterator<String> nameIterator, CoraCaseBase caseBase) {
            this.caseBase = caseBase;
            this.nameIterator = nameIterator;
        }

        /**
         * Wird nicht unterstützt...
         */
        @Override
        public void remove() {
            throw new NotImplementedException();
        }

        /**
         * Gibt zurück, ob ein weiterer Fall vorhanden ist
         * @return
         */
        @Override
        public boolean hasNext() {
            return nameIterator.hasNext();
        }

        /**
         * Gibt den nächsten Fall zurück, oder <code>null</code> wenn kein weiterer
         * Fall gefunden wird (oder ein Fehler aufgetreten ist).
         * @return
         */
        @Override
        public CoraCaseModel next() {
            if(!nameIterator.hasNext()) {
                return null;
            }

            try {
                return caseBase.loadCase(nameIterator.next());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Lokaler Model-Getter. Diese Klasse wird verwendet, um Imports einer Ontologie
     * zu laden. Da die Domain-Ontologie (import) nicht unter der angegebenen Webaddesse
     * verfügbar ist, wird der versuch unterbrochen und die lokale (gecachte) version der
     * Domain-Ontologie verwendet.
     */
    private class LocalModelGetter implements ModelGetter {
        private Map<String, OntModel> mapping;

        public LocalModelGetter() {
            mapping = new HashMap<String, OntModel>();
        }

        /**
         * Fügt eine Modell für den lokalen import hinzu.
         * @param toImport
         */
        public void addModel(OntModel toImport) {
            mapping.put(toImport.getNsPrefixURI(""), toImport);
        }

        /**
         * Nicht verwendete Methode zum Laden von Imports
         * @param s die angeforderte Url
         * @return
         */
        @Override
        public Model getModel(String s) {
            throw new NotImplementedException();
        }

        /**
         * Import Lademethode. Ist die angeforderte Url bekannt, gib die gecachte, lokale Version
         * des Modells zurück. Ist die Url unbekannt, nutze die Standard-Lademethode
         * (via http), um die Ontologie zu laden.
         * @param s die zu ladende Url
         * @param modelReader die Standard-Lademethode
         * @return das geladene Model
         */
        @Override
        public Model getModel(String s, ModelReader modelReader) {
            if(mapping.containsKey(s)) {
                System.out.println("importing domain ontology -> " + s);
                return mapping.get(s);
            }

            System.out.println("unknown ontology import -> " + s);
            Model m = ModelFactory.createDefaultModel();
            modelReader.readModel(m, s);
            return m;
        }
    }
}
