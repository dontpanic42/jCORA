package controllers.cbquery;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import controllers.commons.ThrowableErrorViewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.cbr.CoraCaseBaseImpl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by daniel on 18.03.2015.
 */
public class CbQueryEditorController {

    /**
     * Basis Template-File für den Code-Editor (Wird in einer WebView angezeigt - inkludierte Dadteien
     * werden relativ zur HTML-Datei geladen.
     */
    private static String EDITOR_TEMPLATE_FILE = "views/cbquery/codemirror/editor.html";

    @FXML
    private WebView editorWebView;

    @FXML
    private TableView<ArrayList<String>> resultsTable;

    private static String readTextFile (InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @FXML
    private void initialize() {
        applyEditingTemplate();
    }

    private void applyEditingTemplate() {
        editorWebView.getEngine().load(this.getClass().getClassLoader().getResource(EDITOR_TEMPLATE_FILE).toExternalForm());
    }

    @FXML
    private void onRunQuery() {
        String query = (String ) editorWebView.getEngine().executeScript("editor.getValue();");

        CoraCaseBaseImpl cbi = (CoraCaseBaseImpl) MainApplication.getInstance().getCaseBase();
        Dataset d = cbi.getDataset();

        d.begin(ReadWrite.READ);

        try {
            QueryExecution qe = QueryExecutionFactory.create(query, d);

            int result = 0;
            ResultSet resultSet = qe.execSelect();

            try {
                showResultSet(resultSet);
            } catch (Exception e) {
                ThrowableErrorViewController.showError(e, true);
            }
        } catch (Exception e) {
            ThrowableErrorViewController.showError(e, true);
        } finally {
            d.end();
        }
    }

    private void clearTable() {
        resultsTable.getColumns().clear();
    }

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

    private void buildTableColumns(ArrayList<ArrayList<String>> results, ArrayList<String> cols) {
        ObservableList<ArrayList<String>> data = FXCollections.observableArrayList();
        data.addAll(results);

        for(int i = 0; i < cols.size(); i++) {
            final int colNumber = i;
            TableColumn<ArrayList<String>, String> tc = new TableColumn<>(cols.get(i));
            tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ArrayList<String>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ArrayList<String>, String> param) {
                    return new SimpleStringProperty((param.getValue().get(colNumber)));
                }
            });

            resultsTable.getColumns().add(tc);
        }

        resultsTable.setItems(data);
    }
}
