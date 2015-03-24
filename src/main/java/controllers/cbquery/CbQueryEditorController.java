package controllers.cbquery;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.web.WebView;
import mainapp.MainApplication;
import models.cbr.CoraCaseBaseImpl;
import view.Commons;

import java.util.ArrayList;

/**
 * Controller für den SPARQL-Editor.
 *
 * Zeigt einen SPARQL-Editor und die Ergebnisse einer SPARQL-Anfrage an. Der verwendete Editor
 * ist der "Codemirror"-Editor, der in einer WebView angezeigt wird.
 *
 * Created by daniel on 18.03.2015.
 */
public class CbQueryEditorController {

    /**
     * Basis Template-File für den Code-Editor (Wird in einer WebView angezeigt - inkludierte Dadteien
     * werden relativ zur HTML-Datei geladen.
     */
    private static String EDITOR_TEMPLATE_FILE = "views/cbquery/codemirror/editor.html";

    /**
     * WebView für die Anzeige des SPARQL-Editors. Derzeit scheint es keinen "guten" Code-
     * Editor für JavaFX zugeben, der die SPARQL-Syntax unterstützt.
     */
    @FXML
    private WebView editorWebView;

    /**
     * Tabelle, in der die Ergebnisse einer Anfrage dargestellt werden. Die Spalten
     * der Tabelle werden dynamisch aus den Anfrage-Variablen erzeugt.
     */
    @FXML
    private TableView<ArrayList<String>> resultsTable;

    @SuppressWarnings("unused")
    @FXML
    private void initialize() {
        applyEditingTemplate();
    }

    /**
     * Lädt die durch das Feld <code>EDITOR_TEMPLATE_FILE</code> vorgegebene Template-Datei (üblicherweise
     * im HTML-Format) und lädt diese in die WebView.
     *
     * Die WebView muss von einem <code>File</code> geladen werden - wird das Template als String geladen,
     * können Pfade relativ zum Template (z.B. Styles/JavaScripts) nicht aufgelöst werden.
     */
    private void applyEditingTemplate() {
        String file = this.getClass().getClassLoader().getResource(EDITOR_TEMPLATE_FILE).toExternalForm();
        if(file != null) {
            editorWebView.getEngine().load(file);
        }
    }

    /**
     * Startet die eingegebene Anfrage
     */
    @SuppressWarnings("unused")
    @FXML
    private void onRunQuery() {
        String query = (String ) editorWebView.getEngine().executeScript("editor.getValue();");

        CoraCaseBaseImpl cbi = (CoraCaseBaseImpl) MainApplication.getInstance().getCaseBase();
        Dataset d = cbi.getDataset();

        d.begin(ReadWrite.READ);

        try {
            QueryExecution qe = QueryExecutionFactory.create(query, d);

            ResultSet resultSet = qe.execSelect();

            try {
                showResultSet(resultSet);
            } catch (Exception e) {
                Commons.showException(e);
            }
        } catch (Exception e) {
            Commons.showException(e);
        } finally {
            d.end();
        }
    }

    /**
     * Entfernt alle Spalten (und implizit Daten) aus der Tabelle
     */
    private void clearTable() {
        resultsTable.getColumns().clear();
    }

    /**
     * Zeigt das Ergebnis einer Anfrage in der Ergebnis-Tabelle an
     * @param resultSet Das Ergebnis der SPARQL anfrage
     */
    public void showResultSet(ResultSet resultSet) {
        clearTable();

        ArrayList<ArrayList<String>> results = new ArrayList<>();
        ArrayList<String> cols = new ArrayList<>(resultSet.getResultVars());

        while(resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();

            ArrayList<String> col = new ArrayList<>();
            for(String var : cols) {
                col.add(RDFNodeToString(solution.get(var)));
            }
            results.add(col);
        }

        buildTableColumns(results, cols);
    }

    /**
     * Gibt den Namen eines RDFNodes zurück.
     * Falls der Node gleich <code>null</code> ist, wird ein Strich zurückgegeben ("-")
     * Falls der Node keinen Namen besitzt, wird "BNode" zurückgegeben
     * Falls der Node ein Literal ist, wird der Wert des Literals als String zurückgegeben
     * Sonst wird der Vollständige Name des Nodes zurückgegeben (inkl. Namensraum)
     * @param node Der Node, dessen Name ermittelt werden soll
     * @return Der Name des Nodes
     */
    private String RDFNodeToString(RDFNode node) {
        if(node == null) {
            return "-";
        }

        if(node.isAnon()) {
            return "BNode";
        }

        if(node.isLiteral()) {
            return node.asLiteral().getLexicalForm();
        }

        return node.toString();
    }

    /**
     * Zeigt die zweidimensionale Liste mit Ergebnissen (<code>results</code>) in einer Tablle an.
     * Die Titelzeile wird durch die <code>cols</code>-Liste definiert.
     * @param results Die Ergebnisse, die angezeigt werden sollen
     * @param cols Die Überschriften für die Titelzeile
     */
    private void buildTableColumns(ArrayList<ArrayList<String>> results, ArrayList<String> cols) {
        ObservableList<ArrayList<String>> data = FXCollections.observableArrayList();
        data.addAll(results);

        for(int i = 0; i < cols.size(); i++) {
            final int colNumber = i;

            TableColumn<ArrayList<String>, String> tc = new TableColumn<>(cols.get(i));
            tc.setCellValueFactory((param) -> new SimpleStringProperty((param.getValue().get(colNumber))));

            resultsTable.getColumns().add(tc);
        }

        resultsTable.setItems(data);
    }
}
