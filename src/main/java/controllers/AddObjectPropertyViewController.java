package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.ontology.CoraClassModel;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by daniel on 25.08.14.
 */
public class AddObjectPropertyViewController {

    @FXML
    private ListView<CoraObjectPropertyModel> listProperties;
    private ObservableList<CoraObjectPropertyModel> itemsProperties;
    @FXML
    private TextField txtSearchRelation;

    @FXML
    private ListView<CoraInstanceModel> listInstances;
    private ObservableList<CoraInstanceModel> itemsInstances;
    @FXML
    private TextField txtSearchInstance;

    private CoraInstanceModel model;

    private Stage stage;

    private Set<CoraClassModel> currentClasses;

    @FXML
    public void initialize() {
        listProperties.setPlaceholder(new Label("Keine Relationen vorhanden"));

        listProperties.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<CoraObjectPropertyModel>() {
            @Override
            public void changed(ObservableValue<? extends CoraObjectPropertyModel> observableValue,
                                CoraObjectPropertyModel oldValue,
                                CoraObjectPropertyModel newValue) {
                onSelectProperty(oldValue, newValue);
            }
        });

        listInstances.setPlaceholder(new Label("Keine Instanzen vorhanden"));
    }

    public static void showAddRelation(Stage parent, CoraInstanceModel model) {
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AddObjectPropertyViewController.class
                    .getClassLoader().getResource("views/addObjectPropertyView.fxml"));
            AnchorPane pane = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(parent);

            Scene scene = new Scene(pane);
            stage.setScene(scene);
            stage.show();

            AddObjectPropertyViewController c = loader.getController();
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

    @SuppressWarnings("unused")
    @FXML
    private void onCancel() {
        if(this.stage != null) {
            this.stage.close();
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void onCreateProperty() {
        CoraObjectPropertyModel property = listProperties.getSelectionModel().getSelectedItem();
        CoraInstanceModel object = listInstances.getSelectionModel().getSelectedItem();
        CoraInstanceModel subject = model;

        if(property == null || object == null || subject == null) {
            System.err.println("Es müssen eine Relation und eine Instanz ausgewählt sein");
            return;
        }

        subject.createObjectRelation(property, object);
        stage.close();
    }

    @SuppressWarnings("unused")
    @FXML
    private void onCreateInstance() throws IOException {
        if(currentClasses == null) {
            return;
        }

        CoraInstanceModel instance =
                AddInstanceViewController.showCreateInstance(
                stage,
                currentClasses);

        if(instance != null) {
            itemsInstances.add(instance);
            listInstances.getSelectionModel().select(instance);
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void onSearchInstance() {
        if(txtSearchInstance.getText().equals("")) {
            listInstances.setItems(itemsInstances);
        } else {
            String toSearch = txtSearchInstance.getText().toLowerCase();
            FilteredList<CoraInstanceModel> filteredList;
            filteredList = itemsInstances.filtered( instance -> instance.toString().toLowerCase().contains(toSearch));
            listInstances.setItems(filteredList);

            listInstances.getSelectionModel().selectFirst();
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void onSearchRelation() {
        if(txtSearchRelation.getText().equals("")) {
            listProperties.setItems(itemsProperties);
        } else {
            String toSearch = txtSearchRelation.getText().toLowerCase();
            FilteredList<CoraObjectPropertyModel> filteredList;
            filteredList = itemsProperties.filtered( prop -> prop.toString().toLowerCase().contains(toSearch));
            listProperties.setItems(filteredList);

            listProperties.getSelectionModel().selectFirst();
        }
    }

    private void onSelectProperty(CoraObjectPropertyModel oldProperty,
                                  CoraObjectPropertyModel newProperty) {
        itemsInstances = FXCollections.observableArrayList();

        if(newProperty == null) {
            return;
        }

        Set<CoraClassModel> domainClasses = newProperty.getRangeClasses();
        currentClasses = domainClasses;
        Set<CoraInstanceModel> instances = new HashSet<>();

        for(CoraClassModel clazz : domainClasses) {
            instances.addAll(clazz.getInstances());
        }

        itemsInstances.addAll(instances);
        txtSearchInstance.setText("");
        listInstances.setItems(itemsInstances);
    }

    public void setModel(CoraInstanceModel model) {
        this.model = model;

        itemsProperties = FXCollections.observableArrayList();
        itemsProperties.addAll(model.getAvailableObjectProperties());

        listProperties.setItems(itemsProperties);
        if(itemsProperties.size() > 0) {
            listProperties.getSelectionModel().selectFirst();
        }
    }
}
