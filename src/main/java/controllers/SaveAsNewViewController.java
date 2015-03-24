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
 * Speichert den aktuell Angezeigten Fall mit einer neuen FallID. Damit wird
 * der aktuelle Fall effektiv dupliziert. Der neu angelegte, duplizierte
 * Fall wird in einem neuen Tab im Hauptfenster geöffnet.
 *
 * Created by daniel on 31.08.14.
 */
public class SaveAsNewViewController {
    /**
     * FXML-Datei dieses Dialogs
     */
    private static final String SAVE_AS_NEW_VIEW_FILE = "views/saveAsNewView.fxml";
    /**
     * Der Fall, der gespeichert werden soll
     */
    private CoraCaseModel caseModel;
    /**
     * Die Stage, auf der dieser Dialog angezeigt wird
     */
    private Stage stage;
    /**
     * Textfeld, in das die neue Fall-ID eingegeben werden muss
     */
    @SuppressWarnings("unused")
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

    /**
     * Setzt den Fall, der gespeichert werden soll
     * @param caseModel Der Fall, der gespeichert werden soll
     */
    public void setModel(CoraCaseModel caseModel) {
        this.caseModel = caseModel;
    }

    /**
     * Filtert die eingebene Fall-ID und ersetzt Sonderzeichen under unterstiche.
     * @see controllers.AddInstanceViewController#escapeInstanceName(String)
     * @param name Die eingegebene Fall-ID
     * @return Die Fall-ID ohne Sonderzeichen
     */
    private String escapeInstanceName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * Speichert den Fall unter dem eingebenen Namen
     * @throws IOException
     */
    @SuppressWarnings("unused")
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

    /**
     * Schließt den Dialog ohne zu speichern
     */
    @SuppressWarnings("unused")
    @FXML
    private void onCancel() {
        stage.close();
    }
}
