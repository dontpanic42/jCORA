package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import models.ontology.CoraInstanceModel;

import java.io.IOException;

/**
 * Created by daniel on 31.08.14.
 */
public class SaveAsNewViewController {

    private CoraCaseModel caseModel;
    private Stage stage;

    @FXML
    private TextField txtCaseID;

    public static void showSaveAsNew(Stage parent, CoraCaseModel model) {
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AddObjectPropertyViewController.class
                    .getClassLoader().getResource("views/saveAsNewView.fxml"));
            AnchorPane pane = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(parent);

            Scene scene = new Scene(pane);
            stage.setScene(scene);
            stage.show();

            SaveAsNewViewController c = loader.getController();
            c.setModel(model);
            c.setStage(stage);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setModel(CoraCaseModel caseModel) {
        this.caseModel = caseModel;
    }

    private String escapeInstanceName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }

    @FXML
    private void onSave() throws IOException {
        String caseId = escapeInstanceName(txtCaseID.getText());
        if(caseId.trim().equals("")) {
            System.err.println("Case id nicht angegeben");
            return;
        }

        CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();
        if(caseBase.caseExists(caseId)) {
            System.err.println("Fall existiert schon.");
            return;
        }

        caseBase.saveAsNewCase(caseModel, caseId);
        stage.close();
        MainApplication.getInstance().getMainStage().requestFocus();
        MainApplication.getInstance().getMainAppView().showCase(caseId);
    }

    @FXML
    private void onCancel() {
        stage.close();
    }
}
