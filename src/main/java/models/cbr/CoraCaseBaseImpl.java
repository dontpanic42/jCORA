package models.cbr;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import mainapp.MainApplication;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.naming.ConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Created by daniel on 22.08.14.
 */
public class CoraCaseBaseImpl implements CoraCaseBase {

    /**
     * Pfad zur Standard-Konfiguration
     */
    //private static final String CASE_BASE_PROPERTIES_FILE = "config/casebase.properties";
    private static final String CASE_NS = "http://example.com/case#";

    private final Preferences prefs = Preferences.userNodeForPackage(CoraCaseBase.class);

    //private LocalModelGetter localModelGetter;
    //private OntModel domainModel;
    private Model domainModel;
    private Dataset dataset;
    private OntDocumentManager documentManager;

    //private Properties caseBaseProperties;

    private List<CaseBaseChangeHandler> caseBaseChangeHandlers = new ArrayList<>();

    /**
     * Erzeugt ein neues <code>CoraCaseBaseImpl</code>-Objekt mit der Standard-Konfiguration
     * @throws ConfigurationException Wenn die Konfiguration fehlerhaft ist
     * @throws FileNotFoundException Wenn die Domain-Ontologie oder die Standardkonfiguration nicht gefunden wird
     */
    public CoraCaseBaseImpl() throws ConfigurationException, FileNotFoundException {
//        Properties defaultProperties = new Properties();
//
//        try {
//            InputStream is = this.getClass().getClassLoader().getResourceAsStream(CASE_BASE_PROPERTIES_FILE);
//            //defaultProperties.load(is);
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
//            defaultProperties.load(reader);
//
//            reader.close();
//            is.close();
//        } catch (IOException e) {
//            throw new FileNotFoundException("Keine CaseBase-Konfiguration gefunden");
//        }
//
//        setupCaseBase(defaultProperties);
        setupCaseBase();
    }

//    /**
//     * Erzeugt ein neues <code>CoraCaseBaseImpl</code>-Objekt
//     * @param properties Die Case-Base Konfiguration
//     * @throws ConfigurationException Wenn die Konfiguration fehlerhaft ist
//     * @throws FileNotFoundException Wenn die Domain-Ontologie nicht gefunden wird
//     * @deprecated
//     */
//    public CoraCaseBaseImpl(Properties properties) throws ConfigurationException, FileNotFoundException {
//        //setupCaseBase(properties);
//        setupCaseBase();
//    }

    /**
     * Initialisiert die Case-Base
     * @throws ConfigurationException
     * @throws FileNotFoundException
     */
    //private void setupCaseBase(Properties properties) throws ConfigurationException, FileNotFoundException {
    private void setupCaseBase() throws ConfigurationException, FileNotFoundException {
        //caseBaseProperties = properties;

        domainModel = loadDomainModel();

        //localModelGetter = new LocalModelGetter();
        //localModelGetter.addModel(domainModel);

        dataset = createTDBDataset();
        documentManager = new OntDocumentManager();
    }

    /**
     * Gibt einen Fall mit dem Namen <code>name</code> zurück. Existiert kein Fall mit diesem Namen, wird ein leerer
     * Fall mit diesem Namen erzeugt.
     * @param name Der Name (id) der Ontologie
     * @return Die Ontologie als Cbr-Fall
     * @throws Throwable Exceptions, die beim Laden des Falls auftreten
     */
    @Override
    public CoraCaseModel loadCase(String name) throws Exception {
        dataset.begin(ReadWrite.READ);

        Model tdbModel = dataset.getNamedModel(name);
        Model m = ModelFactory.createDefaultModel();
        m.add(tdbModel);
        //TODO: Prefixes?
        if(tdbModel.getNsPrefixURI("") == null) {
            m.setNsPrefix("", CASE_NS);
            System.out.println("Prefix ist null");
        } else {
            m.setNsPrefix("", tdbModel.getNsPrefixURI(""));
        }

        dataset.end();

        OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, m);
        model.addSubModel(domainModel);

        return new CoraCaseModelImpl(name, model, this);
    }

    @Override
    public void importCase(String asId, String fileFormat, File file) throws IOException {
//        dataset.begin(ReadWrite.WRITE);
//
//        //TODO: Strip existing domain-ontology imports from model!
//        Model m = dataset.getNamedModel(asId);
//
//        try {
//            InputStream is = new FileInputStream(file);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
//
//            m.read(is, fileFormat);
//
//            reader.close();
//            is.close();
//
//            dataset.commit();
//
//            m.close();
//            dataset.end();
//
//            System.out.println("Imported " + asId + " file: " + file.getAbsolutePath() + " format: " + fileFormat);
//            for(CaseBaseChangeHandler h : caseBaseChangeHandlers) {
//                h.onAddCase(asId);
//            }
//        } catch (FileNotFoundException e) {
//            dataset.end();
//            e.printStackTrace();
//            throw e;
//        }

        Model tdbModel = ModelFactory.createDefaultModel();
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
            tdbModel.read(is, fileFormat);

            reader.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        }

        Model m = ModelFactory.createDefaultModel();
        m.add(tdbModel);
        //TODO: Prefixes?
        if(tdbModel.getNsPrefixURI("") == null) {
            m.setNsPrefix("", CASE_NS);
            System.out.println("Prefix ist null");
        } else {
            m.setNsPrefix("", tdbModel.getNsPrefixURI(""));
        }


        OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, m);
        model.addSubModel(domainModel);

        try {
            CoraCaseModelImpl caseModel = new CoraCaseModelImpl(null, model, this);
            saveAsNewCase(caseModel, asId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Erzeugt einen Fall, der nicht in der Datenbank gesichert ist, sonst jedoch
     * alle eigenschaften eines normalen Falls aufweist. Soll zur erstellung der
     * Falleingabe dienen. Ein so erzeugte Fall ist nicht in der Fallbasis vorhanden.
     * @return Ein nicht in der Datenbank gesicherten Fall
     * @throws Throwable Exceptions, die beim Erstellen des Falls auftreten
     */
    @Override
    public CoraCaseModel createTemporaryCase() throws Exception {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefix("", CASE_NS);

        //Reasoner r = PelletReasonerFactory.theInstance().create();


        OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, m);

        model.setNsPrefix("", CASE_NS);
        model.addSubModel(domainModel);

        return new CoraCaseModelImpl(null, model, this);
    }

    /**
     * Erzeugt einen Fall auf Grundlage eines Modells, der nicht in der Datenbank gesichert ist.
     * @return Ein nicht in der Datenbank gesicherten Fall
     * @throws Throwable Exceptions, die beim Erstellen des Falls auftreten
     */
    public CoraCaseModel createTemporaryCase(Model m) throws Exception {
        m.setNsPrefix("", CASE_NS);

        //Reasoner r = PelletReasonerFactory.theInstance().create();


        OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, m);

        model.setNsPrefix("", CASE_NS);
        model.addSubModel(domainModel);

        return new CoraCaseModelImpl(null, model, this);
    }

    /**
     * Gibt zurück, ob ein Fall mit dem Namen <code>name</code> in der Case-Base existiert.
     * @param name Name des zu suchenden Falles
     * @return <code>true</code>, wenn der Fall existiert
     */
    @Override
    public boolean caseExists(String name) {
        dataset.begin(ReadWrite.READ);
        boolean test = dataset.containsNamedModel(name);
        dataset.end();

        return test;
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

//    private String getDomainModelPath() {
//
//        String appPath = MainApplication.getApplicationPath();
//        String relativePath =  caseBaseProperties.getProperty("domainModelFileDefault");
//
//        if(appPath == null || !(new File(appPath + relativePath).exists())) {
//            String fallback = caseBaseProperties.getProperty("domainModelFileFallback");
//            System.out.println("Using Domain-Model: " + fallback);
//            return fallback;
//        } else {
//            System.out.println("Using Domain-Model: " + appPath + relativePath);
//            return appPath + relativePath;
//        }
//    }

    /**
     * Läd die in der Konfiguration (<code>casBaseProperties</code>) hinterlegte Domain-Ontologie
     * @return Die Domänenontologie als Jena-Model
     * @throws ConfigurationException
     * @throws FileNotFoundException
     */
    //private OntModel loadDomainModel()
    private Model loadDomainModel()
            throws ConfigurationException, FileNotFoundException {

        //String domainModelFile = caseBaseProperties.getProperty("domainModelFile");
        //String domainModelFile = getDomainModelPath();
        String domainModelFile = prefs.get("domainModelFile", MainApplication.getApplicationPath() + "domain.owl");

        if(domainModelFile == null) {
            throw new ConfigurationException();
        }

        if(!(new File(domainModelFile).exists())) {
            throw new FileNotFoundException();
        }

        //Model m = RDFDataMgr.loadModel(domainModelFile, Lang.RDFXML);
        Model m = ModelFactory.createDefaultModel();

        try {
            InputStream is = new FileInputStream(domainModelFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));

            //TODO: Checken, ob es nicht m.read(reader... heißen sollte (wenn es zu UTF8-Problemen kommen sollte)
            m.read(is, "RDF/XML-ABBREV", null);

            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        if(caseBaseProperties.getProperty("domainNsOverride", null) != null) {
//            m.setNsPrefix("", caseBaseProperties.getProperty("domainNsOverride"));
//        }
        if(!prefs.get("domainNsOverride", "").equals("")) {
            m.setNsPrefix("", prefs.get("domainNsOverride", ""));
        }

        return m;
    }

    @Override
    public String getDomainNs() {
        return domainModel.getNsPrefixURI("");
    }

    /**
     * (Für Tests: Gibt die Domain-Ontologie zurück.)
     * @return Die Domänenontologie als Jena OntModel
     */
    public OntModel getDomainModel() {
        return ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, domainModel);
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

    public void saveAsFile(CoraCaseModel caseModel, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            CoraCaseModelImpl cimp = (CoraCaseModelImpl) caseModel;
            cimp.getModel().write(os, "RDF/XML-ABBREV", CASE_NS);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAsNewCase(CoraCaseModel caseModel, String name) {
        if(!(caseModel instanceof CoraCaseModelImpl)) {
            System.err.println("Nicht instanceof CoraCaseModeImpl");
            return;
        }

        OntModel original = ((CoraCaseModelImpl) caseModel).getModel();
        Model toSave = original.getBaseModel();

        dataset.begin(ReadWrite.WRITE);

        //dataset.replaceNamedModel(id, toSave);
        dataset.addNamedModel(name, toSave);

        dataset.commit();
        dataset.end();

        for(CaseBaseChangeHandler h : caseBaseChangeHandlers) {
            h.onAddCase(name);
        }
    }

    /**
     * Löscht den Fall mit der Id <code>caseId</code> aus der
     * Fallbasis.
     * @param caseId Die Id des zu löschenden Falls.
     */
    @Override
    public void removeCase(String caseId) {
        dataset.begin(ReadWrite.WRITE);

        dataset.removeNamedModel(caseId);

        dataset.commit();
        dataset.end();

        for(CaseBaseChangeHandler h : caseBaseChangeHandlers) {
            h.onRemoveCase(caseId);
        }
    }

//    private String getTDBDatasetPath() {
//
//        String appPath = MainApplication.getApplicationPath();
//        String relativePath = caseBaseProperties.getProperty("tdbCaseBaseDefault");
//
//        if(appPath == null || !(new File(appPath + relativePath).exists())) {
//            String fallback = caseBaseProperties.getProperty("tdbCaseBaseFallback");
//            System.out.println("Using TDB-Dataset-Model: " + fallback);
//            return fallback;
//        } else {
//            System.out.println("Using TDB-Dataset-Model: " + appPath + relativePath);
//            return appPath + relativePath;
//        }
//    }

    /**
     * Initialisiert die TDB-Datenbank, wie in der Konfiguration (<code>caseBaseProperties</code>)
     * spezifziert
     * @return Das Dataset, das die Datenbank repräsentiert
     * @throws ConfigurationException Wenn die Konfiguration fehlerhaft ist
     */
    private Dataset createTDBDataset()
            throws ConfigurationException {

        //String tdbPath = getTDBDatasetPath();// caseBaseProperties.getProperty("tdbCaseBase");
        String tdbPath = prefs.get("tdbCaseBase", MainApplication.getApplicationPath() + "fallbasis");
        if(tdbPath == null) {
            throw new ConfigurationException("TDB-CaseBase nicht spezifiziert");
        }

        new File(tdbPath).mkdirs();

        return TDBFactory.createDataset(tdbPath);
    }

    @Override
    public void addCaseBaseChangeHandler(CaseBaseChangeHandler handler) {
        caseBaseChangeHandlers.add(handler);
    }

    @Override
    public void removeCaseBaseChangeHandler(CaseBaseChangeHandler handler) {
        caseBaseChangeHandlers.remove(handler);
    }

    public List<String> getCaseIDs() {
        List<String> names = new ArrayList<>();

        dataset.begin(ReadWrite.READ);
        Iterator<String> iter = dataset.listNames();
        while(iter.hasNext()) {
            names.add(iter.next());
        }
        dataset.end();

        return names;
    }

    public Dataset getDataset() {
        return dataset;
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
         * @return <code>true</code>, wenn ein weiterer Fall vorhanden ist
         */
        @Override
        public boolean hasNext() {
            return nameIterator.hasNext();
        }

        /**
         * Gibt den nächsten Fall zurück, oder <code>null</code> wenn kein weiterer
         * Fall gefunden wird (oder ein Fehler aufgetreten ist).
         * @return Der nächste gefundene Fall
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
}
