package controllers.commons;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.Language;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 25.10.14.
 */
public class TranslateStringViewController {

    private static final String TRANSLATE_STRING_VIEW_FILE = "views/commons/translateStringView.fxml";

    @FXML
    private TableView<Translation> tblStrings;

    @FXML
    private TableColumn<Translation, String> columnLanguage;

    @FXML
    private TableColumn<Translation, String> columnString;

    @FXML
    private TableColumn<Translation, Image> columnLangIcon;

    @FXML
    private TextField txtPrimaryString;

    @FXML
    private Label lblPrimaryLanguage;

    @FXML
    private ImageView iconPrimaryLanguage;

    private ObjectProperty<Language> primaryLanguage = new SimpleObjectProperty<>();
    private boolean wasCanceled = false;
    private Stage stage;

    /**
     * Zeigt den Übersetzungsdialog an. Die zurückgegebene Map hat die Form (LanguageTag, ÜbersetzterString).
     * Die Map hat nur ein garantiertes Feld, die Primäre Sprache.
     * @return Eine Map mit Übersetzungen
     * @throws IOException
     */
    public static Map<String, String> getStringTranslation(StringProperty primaryString, Map<String, String> presets) throws IOException {
        FXMLLoader loader = ViewBuilder.getInstance().createLoader(TRANSLATE_STRING_VIEW_FILE);
        Parent parent = loader.load();

        Stage stage = new Stage();
        stage.setTitle(ViewBuilder.getInstance().getText("ui.translate.title"));
        stage.setScene(new Scene(parent));
        TranslateStringViewController controller = loader.getController();

        if(primaryString != null) {
            controller.getPrimaryString().setValue(primaryString.getValue());
            primaryString.bind(controller.getPrimaryString());
        }

        if(presets != null) {
            controller.setFromMap(presets);
        }

        controller.setStage(stage);

        stage.showAndWait();

        if(primaryString != null) {
            primaryString.unbind();
        }

        if(controller.isWasCanceled()) {
            return presets;
        } else {
            return controller.getTranslations();
        }
    }

    @FXML
    private void initialize() {
        Language primary = Language.getLanguageByTag(MainApplication.getInstance().getLanguage());
        primaryLanguage.setValue(primary);
        lblPrimaryLanguage.setText(primaryLanguage.getValue().getName());
        iconPrimaryLanguage.setImage(primary.getIcon());

        setupTable();
    }

    public Map<String, String> getTranslations() {
        Map<String, String> result = new HashMap<>();
        result.put(primaryLanguage.getValue().getTag(), txtPrimaryString.getText());

        for(Translation t : tblStrings.getItems()) {
            if(!t.getString().equals("")) {
                result.put(t.getLanguage().getTag(), t.getString());
            }
        }

        return result;
    }

    public void setFromMap(Map<String, String> strings) {
        ObservableList<Translation> translations = FXCollections.observableArrayList();
        for(Map.Entry<String, String> e : strings.entrySet()) {
            if(e.getKey().equals(primaryLanguage.getValue().getTag())) {
                continue;
            }

            translations.add(new Translation(Language.getLanguageByTag(e.getKey()), e.getValue()));
        }

        tblStrings.setItems(translations);
    }

    private void setupTable() {
        columnLangIcon.setCellValueFactory(translationImageCellDataFeatures ->
                new ReadOnlyObjectWrapper<>(translationImageCellDataFeatures.getValue().getLanguage().getIcon()));

        columnLangIcon.setCellFactory(new Callback<TableColumn<Translation, Image>, TableCell<Translation, Image>>() {
            @Override
            public TableCell<Translation, Image> call(TableColumn<Translation, Image> translationImageTableColumn) {
                return new TableCell<Translation, Image>() {
                    @Override
                    protected void updateItem(Image image, boolean b) {
                        super.updateItem(image, b);

                        if(b) {
                            setGraphic(null);
                        } else {
                            setGraphic(new ImageView(image));
                        }
                    }
                };
            }
        });

        columnLanguage.setCellValueFactory(translationLanguageCellDataFeatures -> {
            final String langName = translationLanguageCellDataFeatures.getValue().languageProperty().getValue().getName();
            return new ReadOnlyObjectWrapper<>(langName);
        });

        columnString.setCellValueFactory(new PropertyValueFactory<>("string"));
        columnString.setCellFactory(TextFieldTableCell.forTableColumn());

        ObservableList<Translation> translations = FXCollections.observableArrayList();
        for(Language lang : Language.values()) {
            if(lang != primaryLanguage.get()) {
                translations.add(new Translation(lang, ""));
            }
        }

        tblStrings.setItems(translations);
    }

    @FXML
    private void onAddRow() {

    }

    @FXML
    private void onRemoveRow() {

    }

    @FXML
    private void onOk() {
        wasCanceled = false;
        stage.close();
    }

    @FXML
    private void onCancel() {
        wasCanceled = true;
        stage.close();
    }

    private StringProperty getPrimaryString() {
        return txtPrimaryString.textProperty();
    }

    public boolean isWasCanceled() {
        return wasCanceled;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public static class Translation {
        private StringProperty string = new SimpleStringProperty();
        private ObjectProperty<Language> language = new SimpleObjectProperty<>();

        public Translation(Language language, String string) {
            this.string.setValue(string);
            this.language.setValue(language);
        }

        public String getString() {
            return string.get();
        }

        public StringProperty stringProperty() {
            return string;
        }

        public void setString(String string) {
            this.string.set(string);
        }

        public Language getLanguage() {
            return language.get();
        }

        public ObjectProperty<Language> languageProperty() {
            return language;
        }

        public void setLanguage(Language language) {
            this.language.set(language);
        }
    }
}
