package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import view.Commons;
import view.viewbuilder.StageInject;
import view.viewbuilder.ViewBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Dialog, der den Fall-Import aus RDF/XML-Dateien konfiguriert und startet.
 *
 * Created by daniel on 24.08.14.
 */
public class CaseImportViewController {
    /**
     * FXML-Datei des Dialogs
     */
    private static final String CASE_IMPORT_VIEW_FILE = "views/caseImportView.fxml";
    /**
     * Import-Button (OK)
     */
    @FXML
    private Button btnImport;
    /**
     * Abbrechen-Button
     */
    @FXML
    private Button btnCancel;
    /**
     * Datei-Auswählen-Button - Öffnet einen <code>FileChooser</code>
     */
    @FXML
    private Button btnSelectFile;
    /**
     * (Deaktiviertes) Text-Feld, das den Speicherort der aktuell ausgewählten
     * RDF/XML-Datei anzeigt
     */
    @FXML
    private TextField txtFile;
    /**
     * Eingabefeld für die FallID des zu importierenden Falls
     */
    @FXML
    private TextField txtCaseID;
    /**
     * ComboBox, die die verschiedenen unterstützten Dateiformate enthält.
     * Derzeit wird nur <code>RDF/XML</code> unterstützt.
     */
    @FXML
    private ComboBox<String> comboFileFormat;
    /**
     * FileChooser für die Auswahl der RDF/XML-Datei
     */
    private final FileChooser fileChooser = new FileChooser();
    /**
     * Die ausgewählte RDF/XML-Datei
     */
    private File file = null;
    /**
     * Die Stage, auf der dieser Dialog angezeigt wird.
     */
    private Stage stage;

    /**
     * Initialisiert den Dialog
     */
    @FXML
    private void initialize() {
        comboFileFormat.getItems().add("RDF/XML");
        comboFileFormat.getSelectionModel().selectFirst();

        btnImport.setOnAction( (ActionEvent e) -> onImport() );
        btnCancel.setOnAction( (ActionEvent e) -> onCancel() );
        btnSelectFile.setOnAction( (ActionEvent e) -> onSelectFile() );
    }

    /**
     * Zeigt den "Fall importieren"-Dialog an
     * @param parent Die Stage, zu der der Dialog modal sein soll
     */
    public static void showCaseImport(Stage parent) {
        ViewBuilder viewBuilder = ViewBuilder.getInstance();
        final String title = viewBuilder.getText("ui.case_import.title");
        viewBuilder.createModal(CASE_IMPORT_VIEW_FILE, parent, title);
    }

    @StageInject
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Zeigt einen <code>FileChooser</code> für die Auswahl der RDF/XML-Datei an und setzt
     * ggf. das Textfeld, in dem der Speicherort angezeigt wird
     */
    private void onSelectFile() {
        file = fileChooser.showOpenDialog(stage);
        if(file == null) {
            txtFile.setText("");
        } else {
            txtFile.setText(file.getAbsolutePath());
        }
    }

    /**
     * Schließt den Dialog
     */
    private void onCancel() {
        stage.close();
    }

    /**
     * Importiert einen Fall aus der ausgewählten Datei.
     * TODO: Fehlerbehandlung
     */
    private void onImport() {
        CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();

        if(file == null || !file.exists()) {
            Commons.showException(new FileNotFoundException("File does not exist"));
            return;
        }

        String caseID = txtCaseID.getText();
        if(caseID.equals("")) {
            Commons.showException(new IllegalArgumentException("Illegal filename"));
            return;
        }

        if(caseBase.caseExists(caseID)) {
            Commons.showException(new IllegalArgumentException("Case already exists"));
            return;
        }

        btnImport.setText("Bitte warten...");

        try {
            caseBase.importCase(caseID, comboFileFormat.getSelectionModel().getSelectedItem(), file);
            stage.close();
        } catch (IOException e) {
            Commons.showException(e);
        }
    }
}
