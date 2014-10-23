package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.ontology.CoraClassModel;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;
import view.viewbuilder.StageInject;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by daniel on 25.08.14.
 */
public class AddObjectPropertyViewController {

    private static final String ADD_OBJECT_PROPERTY_VIEW_FILE = "views/addObjectPropertyView.fxml";

    @FXML
    private ListView<CoraObjectPropertyModel> listProperties;
    private ObservableList<CoraObjectPropertyModel> itemsProperties;
    private Task<ObservableList<CoraObjectPropertyModel>> listPropertiesTask;

    @FXML
    private TextField txtSearchRelation;

    @FXML
    private ListView<CoraInstanceModel> listInstances;
    private ObservableList<CoraInstanceModel> itemsInstances;
    private Task<ObservableList<CoraInstanceModel>> listInstancesTask;

    @FXML
    private TextField txtSearchInstance;

    private CoraInstanceModel model;

    private Stage stage;

    private Set<CoraClassModel> currentClasses;

    @FXML
    public void initialize() {
        String txtNotInitialized = ViewBuilder.getInstance().getText("ui.add_object_property.not_initialized");

        listProperties.setPlaceholder(new Label(txtNotInitialized));

        listProperties.getSelectionModel().selectedItemProperty().addListener((ov, oldProperty, newProperty) ->
            onSelectProperty(oldProperty, newProperty));

        listInstances.setPlaceholder(new Label(txtNotInitialized));
    }

    public static void showAddRelation(Stage parent, CoraInstanceModel model) {
        AddObjectPropertyViewController c = ViewBuilder.getInstance().createModal(ADD_OBJECT_PROPERTY_VIEW_FILE, parent);
        c.setModel(model);
    }

    @StageInject
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

        if(newProperty == null || oldProperty == newProperty) {
            return;
        }

        txtSearchInstance.setText("");
        ProgressIndicator pi = new ProgressIndicator();
        pi.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        pi.setMaxSize(50.0, 50.0);
        listInstances.setPlaceholder(pi);

        if(listInstancesTask != null) {
            listInstancesTask.cancel();
        }

        listInstancesTask = new Task<ObservableList<CoraInstanceModel>>() {
            @Override
            protected ObservableList<CoraInstanceModel> call() throws Exception {
                ObservableList<CoraInstanceModel> localItemsInstances;
                localItemsInstances = FXCollections.observableArrayList();


                Set<CoraClassModel> domainClasses = newProperty.getRangeClasses();
                currentClasses = domainClasses;
                Set<CoraInstanceModel> instances = new HashSet<>();

                for(CoraClassModel clazz : domainClasses) {
                    instances.addAll(clazz.getInstances());
                }

                localItemsInstances.addAll(instances);
                return localItemsInstances;
            }
        };

        listInstancesTask.stateProperty().addListener((ov, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                itemsInstances = listInstancesTask.getValue();
                listInstances.setItems(itemsInstances);
                String txtNoInstances = ViewBuilder.getInstance().getText("ui.add_object_property.label_no_instances");
                listInstances.setPlaceholder(new Label(txtNoInstances));
            }
        });

        new Thread(listInstancesTask).start();
    }

    public void setModel(CoraInstanceModel model) {
        this.model = model;

        ProgressIndicator pi = new ProgressIndicator();
        pi.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        pi.setMaxSize(50.0, 50.0);
        listProperties.setPlaceholder(pi);

        if(listPropertiesTask != null) {
            listPropertiesTask.cancel();
        }

        listPropertiesTask = new Task<ObservableList<CoraObjectPropertyModel>>() {
            @Override
            protected ObservableList<CoraObjectPropertyModel> call() throws Exception {
                ObservableList<CoraObjectPropertyModel> localItemsProperties;
                localItemsProperties = FXCollections.observableArrayList();
                localItemsProperties.addAll(model.getAvailableObjectProperties());
                return localItemsProperties;
            }
        };

        listPropertiesTask.stateProperty().addListener((ov, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                itemsProperties = listPropertiesTask.getValue();
                listProperties.setItems(itemsProperties);
                if(itemsProperties.size() > 0) {
                    listProperties.getSelectionModel().selectFirst();
                }

                String txtNoRelations = ViewBuilder.getInstance().getText("ui.add_object_property.label_no_relations");
                listProperties.setPlaceholder(new Label(txtNoRelations));
            }
        });

        new Thread(listPropertiesTask).start();
    }
}
