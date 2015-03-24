package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import view.viewbuilder.StageInject;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;

/**
 * Created by daniel on 31.08.14.
 */
public class SaveAsNewViewController {

    private static final String SAVE_AS_NEW_VIEW_FILE = "views/saveAsNewView.fxml";

    private CoraCaseModel caseModel;
    private Stage stage;

    @FXML
    private TextField txtCaseID;

    /**
     * Zeigt den Save-As-New Dialog an
     * @param parent Die Stage, zu der dieser Dialog modal sein soll
     * @param model Das Fallmodell, das gespeichert werden soll
     */
    public static void showSaveAsNew(Stage parent, CoraCaseModel model) {
        ViewBuilder viewBuilder = ViewBuilder.getInstance();
        final String title = viewBuilder.getText("ui.save_as_new.title");
        SaveAsNewViewController c = viewBuilder.createModal(SAVE_AS_NEW_VIEW_FILE, parent, title);
        c.setModel(model);
    }

    @StageInject
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
