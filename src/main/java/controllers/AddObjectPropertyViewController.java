package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.ontology.CoraClassModel;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;
import models.ontology.CoraOntologyModel;
import view.viewbuilder.StageInject;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;
import java.util.Collections;
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

        listProperties.setCellFactory(new Callback<ListView<CoraObjectPropertyModel>, ListCell<CoraObjectPropertyModel>>() {
            @Override
            public ListCell<CoraObjectPropertyModel> call(ListView<CoraObjectPropertyModel> coraObjectPropertyModelListView) {
                return new ListCell<CoraObjectPropertyModel>() {
                    @Override
                    protected void updateItem(CoraObjectPropertyModel coraObjectPropertyModel, boolean empty) {
                        super.updateItem(coraObjectPropertyModel, empty);

                        if(empty) {
                            setText("");
                        } else {
                            final String lang = MainApplication.getInstance().getLanguage();
                            setText(coraObjectPropertyModel.getDisplayName(lang));
                        }
                    }
                };
            }
        });

        listInstances.setCellFactory(new Callback<ListView<CoraInstanceModel>, ListCell<CoraInstanceModel>>() {
            @Override
            public ListCell<CoraInstanceModel> call(ListView<CoraInstanceModel> coraInstanceModelListView) {
                return new ListCell<CoraInstanceModel>() {
                    @Override
                    protected void updateItem(CoraInstanceModel coraInstanceModel, boolean empty) {
                        super.updateItem(coraInstanceModel, empty);

                        if (empty) {
                            setText("");
                        } else {
                            final String lang = MainApplication.getInstance().getLanguage();
                            setText(coraInstanceModel.getDisplayName(lang));
                        }
                    }
                };
            }
        });
    }

    /**
     * Zeigt den "AddObjectProperty"-Dialog an
     * @param parent Die Stage, zu dem der Dialog modal sein soll
     * @param model Die Instanz, der ein ObjectProperty hinzugefügt werden soll
     */
    public static void showAddRelation(Stage parent, CoraInstanceModel model) {
        final String dialogTitle = ViewBuilder.getInstance().getText("ui.add_object_property.title");
        AddObjectPropertyViewController c = ViewBuilder.getInstance().createModal(ADD_OBJECT_PROPERTY_VIEW_FILE,
                parent, dialogTitle);
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

    /**
     * Wird aufgerufen, wenn der "Instanz hinzufügen" Button gedrückt wird.
     * @throws IOException Wenn die Instanz-Erzeugen-Dialog FXML Datei nicht gefunden wurde
     */
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
            sortListView(listInstances);
            listInstances.getSelectionModel().select(instance);
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void onSearchInstance() {
        if(txtSearchInstance.getText().equals("")) {
            listInstances.setItems(itemsInstances);
        } else {
            final String lang = MainApplication.getInstance().getLanguage();
            String toSearch = txtSearchInstance.getText().toLowerCase();
            FilteredList<CoraInstanceModel> filteredList;
            filteredList = itemsInstances.filtered( instance -> instance.getDisplayName(lang).toLowerCase().contains(toSearch));
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
            final String lang = MainApplication.getInstance().getLanguage();
            String toSearch = txtSearchRelation.getText().toLowerCase();
            FilteredList<CoraObjectPropertyModel> filteredList;
//            filteredList = itemsProperties.filtered( prop -> prop.toString().toLowerCase().contains(toSearch));
            filteredList = itemsProperties.filtered( prop -> prop.getDisplayName(lang).toLowerCase().contains(toSearch));
            listProperties.setItems(filteredList);

            listProperties.getSelectionModel().selectFirst();
        }
    }

    /**
     * Wird aufgerufen, wenn der Nutzer ein ObjectProperty aus der Liste der ObjectProperties auswählt.
     * Passt die Liste der angezeigten Instanzen an.
     * @param oldProperty das vorher ausgewählte ObjectProperty
     * @param newProperty das jetzt ausgewählte ObjectProperty
     */
    private void onSelectProperty(CoraObjectPropertyModel oldProperty,
                                  CoraObjectPropertyModel newProperty) {

        final String lang = MainApplication.getInstance().getLanguage();
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

                // Filtere alle Instanzer heraus, die keinen Namen besitzen (BNodes)
                for(CoraInstanceModel instance : instances) {
                    if( instance.getDisplayName(lang) != null &&
                       !instance.getDisplayName(lang).trim().equals("")) {
                        localItemsInstances.add(instance);
                    }
                }

                return localItemsInstances;
            }
        };

        listInstancesTask.stateProperty().addListener((ov, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                itemsInstances = listInstancesTask.getValue();
                listInstances.setItems(itemsInstances);
                sortListView(listInstances);
                String txtNoInstances = ViewBuilder.getInstance().getText("ui.add_object_property.label_no_instances");
                listInstances.setPlaceholder(new Label(txtNoInstances));
            } else if(newState == Worker.State.FAILED) {
                listInstancesTask.getException().printStackTrace();
            }
        });

        new Thread(listInstancesTask).start();
    }

    /**
     * Setzt die Instanz, für die ein ObjectProperty hinzugefügt werden soll. Es werden
     * nur diejenigen ObjectProperties angzeigt, welche als Vorbereich die Instanz besitzten (Domain).
     * @param model Die Instanz, der ein ObjectProperty hinzugefügt werden soll
     */
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
                    sortListView(listProperties);
                    listProperties.getSelectionModel().selectFirst();
                }

                String txtNoRelations = ViewBuilder.getInstance().getText("ui.add_object_property.label_no_relations");
                listProperties.setPlaceholder(new Label(txtNoRelations));
            }
        });

        new Thread(listPropertiesTask).start();
    }

    /**
     * Sortiert eine ListView alphabetisch, ohne Berücksichtigung von Groß- und Kleinschreibung
     */
    private void sortListView(ListView listView) {
        final String lang = MainApplication.getInstance().getLanguage();
        Collections.sort(listView.getItems(), (CoraOntologyModel o1, CoraOntologyModel o2)
                -> o1.getDisplayName(lang).compareToIgnoreCase(o2.getDisplayName(lang))
        );
    }
}
