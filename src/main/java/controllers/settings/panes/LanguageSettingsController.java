package controllers.settings.panes;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.Language;

import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * "Einstellungen"-Panel für Spracheinstellungen
 *
 * Created by daniel on 22.03.15.
 */
public class LanguageSettingsController {
    /**
     * Nutzer-Einstellungen
     */
    private Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);
    /**
     * Liste, die die verfügbaren Sprachen enthält
     */
    @FXML
    private ListView<Language> languageList;

    /**
     * Initialisiert das Panel
     */
    @SuppressWarnings("unused")
    @FXML
    private void initialize() {
        languageList.setCellFactory(new Callback<ListView<Language>, ListCell<Language>>() {
            @Override
            public ListCell<Language> call(ListView<Language> languageListView) {
                return new ListCell<Language>() {
                    @Override
                    protected void updateItem(Language language, boolean empty) {
                        super.updateItem(language, empty);

                        if(!empty) {
                            Label label = new Label();
                            label.setText(language.getName());
                            label.setGraphic(new ImageView(language.getIcon()));
                            setGraphic(label);
                        }
                    }
                };
            }
        });

        listLanguages();
        selectCurrentLanguage();
    }

    /**
     * Zeigt alle verfügbaren Sprachen in der <code>languageList</code>
     * ListView an.
     */
    private void listLanguages() {
        for(Language l : Language.values()) {
            languageList.getItems().add(l);
        }
    }

    /**
     * Wählt die aktuelle Sprache in der <code>languageList</code> aus.
     */
    private void selectCurrentLanguage() {
        String tag = MainApplication.getInstance().getLanguage();
        Language lang = Language.getLanguageByTag(tag);
        languageList.getSelectionModel().select(lang);
    }

    /**
     * Speichert die Einstellungen.
     * @see controllers.settings.SettingsViewController#onSave()
     */
    public void save() {
        Language selected = languageList.getSelectionModel().getSelectedItem();
        if(selected == null) {
            System.err.println("No language selected");
            return;
        }

        prefs.put("language", selected.getTag());
    }

    /**
     * Setzt alle Einstellungen auf Standardwerte zurück. D.h. es wird die Systemsprache
     * verwendet (falls verfügbar) oder die <code>DEFAULT_LANGUAGE</code> der Anwendung.
     * @see java.util.Locale#getDefault()
     * @see mainapp.MainApplication#DEFAULT_LANGUAGE
     */
    public void reset() {
        String def = Locale.getDefault().getLanguage();
        Language lang = Language.getLanguageByTag(def);
        // Die Systemsprache ist nicht als Sprache verfügbar
        if(lang == null) {
            // Benutzte die Standardsprache
            lang = MainApplication.DEFAULT_LANGUAGE;
        }

        languageList.getSelectionModel().select(lang);
    }
}
