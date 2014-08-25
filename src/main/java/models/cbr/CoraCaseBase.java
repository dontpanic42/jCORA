package models.cbr;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by daniel on 22.08.14.
 */
public interface CoraCaseBase {

    public interface CaseBaseChangeHandler {
        public void onAddCase(String caseId);

        public void onRemoveCase(String caseId);
    }

    /**
     * Gibt einen Fall mit dem Namen <code>name</code> zurück. Existiert kein Fall mit diesem Namen, wird ein leerer
     * Fall mit diesem Namen erzeugt.
     * @param name Der Name (id) der Ontologie
     * @return Die Ontologie als Cbr-Fall
     * @throws Throwable
     */
    public CoraCaseModel loadCase(String name) throws Throwable;

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
     * Erzeugt einen Fall, der nicht in der Datenbank gesichert ist, sonst jedoch
     * alle eigenschaften eines normalen Falls aufweist. Soll zur erstellung der
     * Falleingabe dienen. Ein so erzeugte Fall ist nicht in der Fallbasis vorhanden.
     * @return Ein nicht in der Datenbank gesicherten Fall
     * @throws Throwable
     */
    public CoraCaseModel createTemporaryCase() throws Throwable;

    public void addCaseBaseChangeHandler(CaseBaseChangeHandler handler);

    public void removeCaseBaseChangeHandler(CaseBaseChangeHandler handler);
}
