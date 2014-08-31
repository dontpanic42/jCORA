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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.ontology.CoraClassModel;
import models.ontology.CoraInstanceModel;
import models.ontology.CoraObjectPropertyModel;

import java.io.IOException;
import java.util.Set;

/**
 * Created by daniel on 25.08.14.
 */
public class AddInstanceViewController {

    @FXML
    private TreeView<CoraClassModel> classTree;

    @FXML
    private TextField txtInstanceName;

    @FXML
    private TextField txtClassName;
    private CoraInstanceModel returnValue = null;
    private Stage stage;

    public static CoraInstanceModel showCreateInstance(Stage parent, Set<CoraClassModel> superClasses) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(AddInstanceViewController.class
                .getClassLoader().getResource("views/addInstanceView.fxml"));
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

        CoraInstanceModel instance = c.getReturnValue();

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


    }

    private void onTreeSelectionChange(CoraClassModel oldValue, CoraClassModel newValue) {
        if(newValue != null) {
            txtClassName.setText(newValue.toString());
        } else {
            txtClassName.setText("(Klasse wählen)");
        }
    }

    @FXML
    private void onCancel() {
        this.stage.close();
    }

    private String escapeInstanceName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }

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
            CoraInstanceModel instance = clazz.getFactory().createInstance(clazz, instanceName);
            setReturnValue(instance);
            stage.close();
        } catch (ResourceAlreadyExistsException e) {
            System.err.println("Instanz existiert bereits");
            return;
        }


    }

    public class ClassTreeItem extends TreeItem<CoraClassModel> {

        private boolean isFirstRequest = true;
        private boolean isLeafNode = false;

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
