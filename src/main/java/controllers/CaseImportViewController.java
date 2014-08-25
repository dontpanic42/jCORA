package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;

import java.io.File;
import java.io.IOException;

/**
 * Created by daniel on 24.08.14.
 */
public class CaseImportViewController {

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
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(CaseImportViewController.class.getClassLoader().getResource("views/caseImportView.fxml"));
            AnchorPane pane = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(parent);

            Scene scene = new Scene(pane);
            stage.setScene(scene);
            stage.show();

            CaseImportViewController c = loader.getController();
            c.setStage(stage);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

    private void setStage(Stage stage) {
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
            System.err.println("Datei existiert nicht");
            return;
        }

        String caseID = txtCaseID.getText();
        if(caseID.equals("")) {
            System.err.println("Keine CaseID");
            return;
        }

        if(caseBase.caseExists(caseID)) {
            System.err.println("CaseID existiert schon");
            return;
        }

        btnImport.setText("Bitte warten...");

        try {
            caseBase.importCase(caseID, comboFileFormat.getSelectionModel().getSelectedItem(), file);
            stage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
