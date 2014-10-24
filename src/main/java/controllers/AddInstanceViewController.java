package controllers;

import exceptions.ResourceAlreadyExistsException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.ontology.CoraClassModel;
import models.ontology.CoraInstanceModel;
import view.viewbuilder.StageInject;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;
import java.util.Set;

/**
 * Created by daniel on 25.08.14.
 */
public class AddInstanceViewController {

    private static final String ADD_INSTANCE_VIEW_FILE = "views/addInstanceView.fxml";

    @FXML
    private TreeView<CoraClassModel> classTree;

    @FXML
    private TextField txtInstanceName;

    @FXML
    private TextField txtInstanceLabel;

    @FXML
    private TextField txtClassName;
    private CoraInstanceModel returnValue = null;
    private Stage stage;

    public static CoraInstanceModel showCreateInstance(Stage parent, Set<CoraClassModel> superClasses) throws IOException {
        FXMLLoader loader = ViewBuilder.getInstance().createLoader(ADD_INSTANCE_VIEW_FILE);
        AnchorPane pane = loader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parent);

        Scene scene = new Scene(pane);
        stage.setScene(scene);

        AddInstanceViewController c = loader.getController();
        c.setSuperClasses(superClasses);
        c.setStage(stage);

        stage.showAndWait();

        return c.getReturnValue();
    }

    public CoraInstanceModel getReturnValue() {
        return returnValue;
    }

    private void setReturnValue(CoraInstanceModel instance) {
        returnValue = instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setSuperClasses(Set<CoraClassModel> superClasses) {
        TreeItem<CoraClassModel> dummyRoot = new TreeItem<>();

        for(CoraClassModel model : superClasses) {
            ClassTreeItem item = new ClassTreeItem(model);
            dummyRoot.getChildren().add(item);
        }

        classTree.setRoot(dummyRoot);
    }

    @FXML
    public void initialize() {
        classTree.setShowRoot(false);
        classTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<CoraClassModel>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<CoraClassModel>> observableValue,
                                TreeItem<CoraClassModel> oldValue,
                                TreeItem<CoraClassModel> newValue) {
                CoraClassModel mOld = (oldValue != null)?
                        newValue.valueProperty().get() : null;
                CoraClassModel mNew = (newValue != null)?
                        newValue.valueProperty().get() : null;

                onTreeSelectionChange(mOld, mNew);
            }
        });
        classTree.setCellFactory(new Callback<TreeView<CoraClassModel>, TreeCell<CoraClassModel>>() {
            @Override
            public TreeCell<CoraClassModel> call(TreeView<CoraClassModel> coraClassModelTreeView) {
                return new TreeCell<CoraClassModel>() {
                    @Override
                    protected void updateItem(CoraClassModel coraClassModel, boolean empty) {
                        super.updateItem(coraClassModel, empty);

                        if(empty) {
                            setText("");
                        } else {
                            final String lang = MainApplication.getInstance().getLanguage();
                            setText(coraClassModel.getDisplayName(lang));
                        }
                    }
                };
            }
        });

    }

    private void onTreeSelectionChange(CoraClassModel oldValue, CoraClassModel newValue) {
        if(newValue != null) {
            txtClassName.setText(newValue.toString());
        } else {
            txtClassName.setText("(Klasse wählen)");
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void onCancel() {
        this.stage.close();
    }

    private String escapeInstanceName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }

    @SuppressWarnings("unused")
    @FXML
    private void onCreateInstance() {
        CoraClassModel clazz = classTree.getSelectionModel()
                .getSelectedItem().valueProperty().get();

        if(clazz == null) {
            System.err.println("Keine klasse ausgewählt.");
            return;
        }

        String instanceName = escapeInstanceName(txtInstanceName.getText());

        if(instanceName.equals("")) {
            System.err.println("Kein Instanzname angegeben");
            return;
        }

        try {
            CoraInstanceModel instance;
            if(txtInstanceLabel.getText().equals("")) {
                //Wenn kein Label angegeben wurde, erzeuge Instanz ohne Label
                instance = clazz.getFactory().createInstance(clazz, instanceName);
            } else {
                //Wenn ein Label angegeben wurde, nehme an, das dies in der aktuellen Sprache ist
                final String lang = MainApplication.getInstance().getLanguage();
                instance = clazz.getFactory().createInstance(clazz, instanceName, txtInstanceLabel.getText(), lang);
            }
            setReturnValue(instance);
            stage.close();
        } catch (ResourceAlreadyExistsException e) {
            System.err.println("Instanz existiert bereits");
            return;
        }


    }

    public class ClassTreeItem extends TreeItem<CoraClassModel> {

        private boolean isFirstRequest = true;

        public ClassTreeItem(CoraClassModel model) {
            valueProperty().setValue(model);
        }

        @Override
        public ObservableList<TreeItem<CoraClassModel>> getChildren() {
            if(isFirstRequest) {
                isFirstRequest = false;
                super.getChildren().setAll(buildChildren());
            }

            return super.getChildren();
        }

        public ObservableList<TreeItem<CoraClassModel>> buildChildren() {
            ObservableList<TreeItem<CoraClassModel>> children;
            Set<CoraClassModel> set = valueProperty().get().getFlattenedChildren();
            children = FXCollections.observableArrayList();

            for(CoraClassModel c : set) {
                children.add(new ClassTreeItem(c));
            }

            return children;
        }

        @Override
        public boolean isLeaf() {
            if(isFirstRequest) {
                isFirstRequest = false;
                super.getChildren().setAll(buildChildren());
            }

            return (super.getChildren().size() == 0);
        }
    }
}
