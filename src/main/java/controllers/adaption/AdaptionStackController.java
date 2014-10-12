package controllers.adaption;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import services.adaption.rules.AdaptionRule;
import services.adaption.rules.GlobalAdaptionRule;
import services.adaption.rules.LocalAdaptionRule;
import services.adaption.rules.impl.local.CopyRule;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by daniel on 09.10.14.
 */
public class AdaptionStackController {

    @FXML
    private ListView<AdaptionRule> listAvailable;

    @FXML
    private ListView<AdaptionRule> listSelected;

    private Stage stage;

    private SimpleObjectProperty<EventHandler<StartAdaptionEvent>> onStartAdaption = new SimpleObjectProperty<>();

    @SuppressWarnings("unused")
    @FXML
    private void initialize() {
        listAvailable.setPlaceholder(new Label("Keine Regeln vorhanden."));
        listSelected.setPlaceholder(new Label("Keine Regeln ausgewählt"));


        Callback<ListView<AdaptionRule>, ListCell<AdaptionRule>> cellFactory = new Callback<ListView<AdaptionRule>, ListCell<AdaptionRule>>() {
            @Override
            public ListCell<AdaptionRule> call(ListView<AdaptionRule> adaptionRuleListView) {
                return new ListCell<AdaptionRule>() {
                    @Override
                    protected void updateItem(AdaptionRule adaptionRule, boolean empty) {
                        super.updateItem(adaptionRule, empty);
                        if(adaptionRule != null) {
                            setGraphic(createDisplay(adaptionRule));
                        } else {
                            setGraphic(createEmptyDisplay());
                        }
                    }

                    private Node createDisplay(AdaptionRule rule) {
                        HBox hbox = new HBox();
                        InputStream is = this.getClass().getClassLoader().getResourceAsStream("icons/adapt.png");
                        ImageView icon = new ImageView(new Image(is));
                        icon.setFitWidth(32.0);
                        icon.setFitHeight(32.0);

                        VBox vbox = new VBox();
                        Label name = new Label(rule.getRuleName());

                        Label description = new Label(rule.getRuleDescription());
                        description.setStyle("-fx-font-size:10;");

                        vbox.getChildren().add(name);
                        vbox.getChildren().add(description);

                        hbox.getChildren().addAll(icon, vbox);

                        return hbox;
                    }

                    private Node createEmptyDisplay() {
                        return new Label("");
                    }
                };
            }
        };

        listAvailable.setCellFactory(cellFactory);
        listSelected.setCellFactory(cellFactory);

        addAvailableRules();
    }

    private void addAvailableRules() {
        listAvailable.getItems().add(new CopyRule());
    }

    /**
     * Der Nutzer schiebt eine Regel aus der 'Vorhanden' Liste in die 'Selected' Liste
     */
    @SuppressWarnings("unused")
    @FXML
    private void onMoveRight() {
        AdaptionRule rule = listAvailable.getSelectionModel().getSelectedItem();
        if(rule == null) {
            return;
        }

        listAvailable.getItems().remove(rule);
        listSelected.getItems().add(rule);
        listSelected.getSelectionModel().select(rule);
    }

    /**
     * Der Nutzer schiebt eine Regel aus der 'Selected' Liste in die 'Vorhanden' Liste.
     */
    @SuppressWarnings("unused")
    @FXML
    private void onMoveLeft() {
        AdaptionRule rule = listSelected.getSelectionModel().getSelectedItem();
        if(rule == null) {
            return;
        }

        listSelected.getItems().remove(rule);
        listAvailable.getItems().add(rule);
        listAvailable.getSelectionModel().select(rule);
    }

    /**
     * Eine Regel soll höher in der Hierarchie stehen
     */
    @SuppressWarnings("unused")
    @FXML
    private void onMoveUp() {
        AdaptionRule rule = listSelected.getSelectionModel().getSelectedItem();
        if(rule == null) {
            return;
        }

        int index = listSelected.getItems().indexOf(rule);
        if(index <= 0) {
            return;
        }

        AdaptionRule prevRule = listSelected.getItems().get(index - 1);
        listSelected.getItems().set(index, prevRule);
        listSelected.getItems().set(index - 1, rule);
        listSelected.getSelectionModel().select(rule);
    }

    /**
     * Eine Regel soll in der Hierarchie weiter unten stehen
     */
    @SuppressWarnings("unused")
    @FXML
    private void onMoveDown() {
        AdaptionRule rule = listSelected.getSelectionModel().getSelectedItem();
        if(rule == null) {
            return;
        }

        int index = listSelected.getItems().indexOf(rule);
        if(index == -1 || index >= (listSelected.getItems().size() - 1) ) {
            return;
        }

        AdaptionRule nextRule = listSelected.getItems().get(index + 1);
        listSelected.getItems().set(index, nextRule);
        listSelected.getItems().set(index + 1, rule);
        listSelected.getSelectionModel().select(rule);
    }

    /**
     * OK-Button
     */
    @SuppressWarnings("unused")
    @FXML
    private void onApply() {
        if(onStartAdaption.getValue() != null) {
            StartAdaptionEvent e = new StartAdaptionEvent(listSelected.getItems());
            onStartAdaption.getValue().handle(e);
        }

        if(stage != null) {
            stage.close();
        }
    }

    /**
     * Abbrechen-Button
     */
    @SuppressWarnings("unused")
    @FXML
    private void onCancel() {
        if(stage != null) {
            stage.close();
        }
    }

    public void setOnStartAdaption(EventHandler<StartAdaptionEvent> eventHandler) {
        onStartAdaption.setValue(eventHandler);
    }

    public EventHandler<StartAdaptionEvent> getOnStartAdaption() {
        return onStartAdaption.getValue();
    }

    public SimpleObjectProperty<EventHandler<StartAdaptionEvent>> OnStartAdaptionProperty() {
        return onStartAdaption;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }



    public class StartAdaptionEvent extends ActionEvent {
        private List<AdaptionRule> ruleStack;

        public StartAdaptionEvent(List<AdaptionRule> ruleStack) {
            super(null, null);
            this.ruleStack = ruleStack;
        }

        public List<AdaptionRule> getRuleStack() {
            return ruleStack;
        }

        public void setRuleStack(List<AdaptionRule> ruleStack) {
            this.ruleStack = ruleStack;
        }
    }
}
