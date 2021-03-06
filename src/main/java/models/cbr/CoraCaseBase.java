package models.cbr;

import com.hp.hpl.jena.rdf.model.Model;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by daniel on 22.08.14.
 */
public interface CoraCaseBase {

    public interface CaseBaseChangeHandler {
        public void onAddCase(String caseId);

        public void onRemoveCase(String caseId);
    }

    /**
     * Gibt den Namespace der Domain-Ontologie zurück.
     * @return Namespace der Domain-Ontologie
     */
    public String getDomainNs();

    /**
     * Gibt einen Fall mit dem Namen <code>name</code> zurück. Existiert kein Fall mit diesem Namen, wird ein leerer
     * Fall mit diesem Namen erzeugt.
     * @param name Der Name (id) der Ontologie
     * @return Die Ontologie als Cbr-Fall
     * @throws Throwable Exceptions, die beim laden des Falls aufgetreten sind
     */
    public CoraCaseModel loadCase(String name) throws Exception;

    /**
     * Gibt zurück, ob ein Fall mit dem Namen <code>name</code> in der Fallbasis existiert.
     * @param name Name des zu suchenden Falles
     * @return <code>true</code>, wenn der Fall existiert
     */
    public boolean caseExists(String name);

    /**
     * Gibt einen Iterator zurück, der es ermöglicht, über alle in der Fallbasis vorhandenen Fälle zu iterieren.
     * @return Iterator über alle Fälle
     */
    public Iterator<CoraCaseModel> getCases();

    public void importCase(String asId, String fileFormat, File file) throws IOException;

    /**
     * Gibt einen Iterator zurück, der über die IDs aller Fälle iteriert.
     * @return Iterator über alle Fall-IDs
     */
    public Iterator<String> listCaseIDs();

    public List<String> getCaseIDs();

    /**
     * Speichert den Fall unter einem neuen Namen
     * @param caseModel Der zu speichernde Fall
     * @param name Der Name (FallID), unter dem der Fall gespeichert wird
     */
    public void saveAsNewCase(CoraCaseModel caseModel, String name);

    /**
     * Speichert den Fall in einer RDF/XML-Datei
     * @param caseModel Der zu speichernde Fall
     * @param file Speicherort der RDF/XML-Datei
     */
    public void saveAsFile(CoraCaseModel caseModel, File file);

    /**
     * Gibt die Anzahl der in dieser Fallbasis gespeicherten Fälle zurück.
     * @return Anzahl der in dieser Fallbasis gespeicherten Fälle
     */
    public int size();

    /**
     * Schließt die Fallbasis. Muss unbedingt vor dem Verlassen der Anwendung aufgerufen werden, da sonst die
     * Korruption der Fallbasis droht.
     */
    public void close();

    /**
     * Löscht den Fall mit der Id <code>caseId</code> aus der
     * Fallbasis.
     * @param caseId Die Id des zu löschenden Falls.
     */
    public void removeCase(String caseId);

    /**
     * Erzeugt einen Fall, der nicht in der Datenbank gesichert ist, sonst jedoch
     * alle eigenschaften eines normalen Falls aufweist. Soll zur erstellung der
     * Falleingabe dienen. Ein so erzeugte Fall ist nicht in der Fallbasis vorhanden.
     * @return Ein nicht in der Datenbank gesicherten Fall
     * @throws Throwable Exceptions, die beim Erstellen des Falls auftreten
     */
    public CoraCaseModel createTemporaryCase() throws Exception;

    /**
     * Erzeugt einen Fall auf Grundlage eines Modells, der nicht in der Datenbank gesichert ist.
     * @return Ein nicht in der Datenbank gesicherten Fall
     * @throws Throwable Exceptions, die beim Erstellen des Falls auftreten
     */
    public CoraCaseModel createTemporaryCase(Model m) throws Exception;

    public void save(CoraCaseModel model);

    public void addCaseBaseChangeHandler(CaseBaseChangeHandler handler);

    public void removeCaseBaseChangeHandler(CaseBaseChangeHandler handler);

}
