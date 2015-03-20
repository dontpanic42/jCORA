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
 * Created by daniel on 24.08.14.
 */
public class CaseImportViewController {

    private static final String CASE_IMPORT_VIEW_FILE = "views/caseImportView.fxml";

    @FXML
    private Button btnImport;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSelectFile;

    @FXML
    private TextField txtFile;

    @FXML
    private TextField txtCaseID;

    @FXML
    private ComboBox<String> comboFileFormat;

    private final FileChooser fileChooser = new FileChooser();
    private File file = null;
    private Stage stage;

    @FXML
    private void initialize() {
        comboFileFormat.getItems().add("RDF/XML");
        comboFileFormat.getSelectionModel().selectFirst();

        btnImport.setOnAction( (ActionEvent e) -> onImport() );
        btnCancel.setOnAction( (ActionEvent e) -> onCancel() );
        btnSelectFile.setOnAction( (ActionEvent e) -> onSelectFile() );
    }

    public static void showCaseImport(Stage parent) {
       ViewBuilder.getInstance().createModal(CASE_IMPORT_VIEW_FILE, parent);

    }

    @StageInject
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void onSelectFile() {
        file = fileChooser.showOpenDialog(stage);
        if(file == null) {
            txtFile.setText("");
        } else {
            txtFile.setText(file.getAbsolutePath());
        }
    }

    private void onCancel() {
        stage.close();
    }

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
