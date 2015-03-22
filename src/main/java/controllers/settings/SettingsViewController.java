package controllers.settings;

import controllers.commons.ThrowableErrorViewController;
import controllers.settings.panes.CaseBaseSettingsViewController;
import controllers.settings.panes.CaseStructureViewController;
import controllers.settings.panes.LanguageSettingsController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mainapp.MainApplication;
import view.Commons;
import view.viewbuilder.ViewBuilder;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Created by daniel on 20.03.15.
 */
public class SettingsViewController {

    private static final String SETTINGS_VIEW_FILE = "views/settings/settingsView.fxml";

    private final Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);

    public Stage stage;
    // Wenn <code>true</code>, lasse den Anwendungsneustart vom Nutzer bestätigen
    // (Wird benötigt, damit beim ersten Anwendungsstart nicht nach einem Neustart gefragt wird)
    public boolean confirmRestart = true;

    @FXML
    private CaseStructureViewController caseStructureViewController;

    @FXML
    private CaseBaseSettingsViewController caseBaseSettingsViewController;

    @FXML
    private LanguageSettingsController languageSettingsViewController;

    public static void showSettings() {
        showSettings(false, true);
    }

    public static void showSettings(boolean block, boolean confirmRestart) {
        try {
            FXMLLoader loader = ViewBuilder.getInstance().createLoader(SETTINGS_VIEW_FILE);
            AnchorPane pane = loader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(pane);
            stage.setScene(scene);

            SettingsViewController controller = loader.getController();
            controller.stage = stage;
            controller.confirmRestart = confirmRestart;

            stage.setTitle(ViewBuilder.getInstance().getText("ui.settings.title"));


            if(block) {
                stage.showAndWait();
            } else {
                stage.show();
            }

        } catch (Exception e) {
            Commons.showFatalException(e);
        }
    }

    @FXML
    private void initialize() {
    }

    /**
     * Fragt den Nutzer, ob die Änderungen gespeichert werden sollen und weist darauf hinm
     * das, falls ja, die Anwendung neu gestartet werden muss.
     * @return <code>true</code>, falls die Änderungen gespeichert werden sollen
     */
    private boolean confirmRestartOnSave() {
        if(!confirmRestart) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(ViewBuilder.getInstance().getText("ui.settings.confirm_restart.title"));
        alert.setHeaderText(ViewBuilder.getInstance().getText("ui.settings.confirm_restart.header"));
        alert.setContentText(ViewBuilder.getInstance().getText("ui.settings.confirm_restart.text"));

        Optional<ButtonType> result = alert.showAndWait();
        return (result.get() == ButtonType.OK);
    }

    @FXML
    private void onSave() {
        if(!confirmRestartOnSave()) {
            return;
        }

        caseStructureViewController.save();
        caseBaseSettingsViewController.save();
        languageSettingsViewController.save();

        /* Ist "AppIsConfigured" nicht gesetzt, so wird dieser Dialog beim Starten
         * der Anwendung angezeigt...
         */
        prefs.putBoolean("AppIsConfigured" + MainApplication.VERSION_STRING, true);
        stage.close();
    }

    @FXML
    private void onCancel() {
        stage.close();
    }

    @FXML
    private void onReset() {
        caseStructureViewController.reset();
        caseBaseSettingsViewController.reset();
        languageSettingsViewController.reset();
    }
}
