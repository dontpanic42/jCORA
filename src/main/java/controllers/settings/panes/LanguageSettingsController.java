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
 * Created by daniel on 22.03.15.
 */
public class LanguageSettingsController {

    private Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);

    @FXML
    private ListView<Language> languageList;

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

    private void listLanguages() {
        for(Language l : Language.values()) {
            languageList.getItems().add(l);
        }
    }

    private void selectCurrentLanguage() {
        String tag = MainApplication.getInstance().getLanguage();
        Language lang = Language.getLanguageByTag(tag);
        languageList.getSelectionModel().select(lang);
    }

    public void save() {
        Language selected = languageList.getSelectionModel().getSelectedItem();
        if(selected == null) {
            System.err.println("No language selected");
            return;
        }

        prefs.put("language", selected.getTag());
    }

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
