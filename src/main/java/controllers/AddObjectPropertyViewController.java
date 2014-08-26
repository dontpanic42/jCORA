package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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

/**
 * Created by daniel on 25.08.14.
 */
public class AddObjectPropertyViewController {

    @FXML
    private ListView<CoraObjectPropertyModel> listProperties;

    @FXML
    private ListView<CoraInstanceModel> listInstances;

    private CoraInstanceModel model;

    private Stage stage;

    private Set<CoraClassModel> currentClasses;

    @FXML
    public void initialize() {
        listProperties.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<CoraObjectPropertyModel>() {
            @Override
            public void changed(ObservableValue<? extends CoraObjectPropertyModel> observableValue,
                                CoraObjectPropertyModel oldValue,
                                CoraObjectPropertyModel newValue) {
                onSelectProperty(oldValue, newValue);
            }
        });


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

    @FXML
    private void onCancel() {
        if(this.stage != null) {
            this.stage.close();
        }
    }

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
            listInstances.getItems().add(instance);
            listInstances.getSelectionModel().select(instance);
        }
    }

    private void onSelectProperty(CoraObjectPropertyModel oldProperty,
                                  CoraObjectPropertyModel newProperty) {
        if(newProperty == null) {
            return;
        }

        Set<CoraClassModel> domainClasses = newProperty.getRangeClasses();
        currentClasses = domainClasses;
        Set<CoraInstanceModel> instances = new HashSet<>();

        for(CoraClassModel clazz : domainClasses) {
            instances.addAll(clazz.getInstances());
        }

        ObservableList<CoraInstanceModel> list = FXCollections.observableArrayList();
        list.addAll(instances);
        listInstances.setItems(list);
    }

    public void setModel(CoraInstanceModel model) {
        this.model = model;

        ObservableList<CoraObjectPropertyModel> properties = FXCollections.observableArrayList();
        properties.addAll(model.getAvailableObjectProperties());

        listProperties.setItems(properties);
        if(properties.size() > 0) {
            listProperties.getSelectionModel().selectFirst();
        }
    }
}
