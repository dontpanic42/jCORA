package controllers.settings;

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
 * Controller des "Einstellungen"-Dialogs
 *
 * Created by daniel on 20.03.15.
 */
public class SettingsViewController {
    /**
     * FXML-Datei des Dialogs
     */
    private static final String SETTINGS_VIEW_FILE = "views/settings/settingsView.fxml";
    /**
     * Einstellungen der <code>MainApplication</code>-Klasse. Werden die Einstellungen gespeichert,
     * wird das "AppIsConfigured"+VERSION_STRING Property gesetzt, so das der Dialog beim nächsten
     * Anwendungsstart nicht automatisch angezeigt wird.
     */
    private final Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);
    /**
     * Die Stage, auf der dieser Dialog angzeigt wird.
     */
    public Stage stage;
    /**
     * Wenn <code>true</code>, lasse den Anwendungsneustart vom Nutzer bestätigen
     * (Wird benötigt, damit beim ersten Anwendungsstart nicht nach einem Neustart gefragt wird)
     */
    public boolean confirmRestart = true;
    /**
     * Controller des Panels für die Fall-Struktur Einstellungen
     */
    @FXML
    private CaseStructureViewController caseStructureViewController;
    /**
     * Controller des Panles für die Fallbasis Einstellungen
     */
    @FXML
    private CaseBaseSettingsViewController caseBaseSettingsViewController;
    /**
     * Controller des Panels für die Spracheinstellungen
     */
    @FXML
    private LanguageSettingsController languageSettingsViewController;

    /**
     * Zeigt den Anwendungsdialog mit Standardeinstellungen. D.h. der Dialog blockiert nicht
     * und es wird beim Speichern der Einstellungen eine Warnung angezeigt die Anwendung
     * neu zu starten.
     *
     * Dies ist die Methode, über die der Dialog mittels des "Einstellungen"-Menüpunkts jederzeit
     * aufgerufen werden kann.
     */
    public static void showSettings() {
        showSettings(false, true);
    }

    /**
     * Zeigt den Dialog mit erweiterten Optionen an.
     * @see SettingsViewController#showSettings()
     * @param block Wenn <code>true</code> blockiert der Dialog
     * @param confirmRestart Wenn <code>true</code> wird nach Speichern der Einstellungen eine Warnung
     *                       angezeigt die Anwendung neu zu starten. Wird der Dialog beim erstmaligen
     *                       Starten der Anwendung angezeigt, muss keine Warnung ausgegeben werden (da
     *                       die Anwendung noch nicht initialisiert ist)
     */
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

    /**
     * Speichert die Einstellungen (ggf. nach dem eine Neu-Starten-Warnung angezeigt wurde)
     */
    @SuppressWarnings("unused")
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

    /**
     * Schließt den Dialog, ohne Änderungen zu speichern.
     */
    @SuppressWarnings("unused")
    @FXML
    private void onCancel() {
        stage.close();
    }

    /**
     * Setzt alle Einstellungen auf Standardwerte zurück
     */
    @SuppressWarnings("unused")
    @FXML
    private void onReset() {
        caseStructureViewController.reset();
        caseBaseSettingsViewController.reset();
        languageSettingsViewController.reset();
    }
}
