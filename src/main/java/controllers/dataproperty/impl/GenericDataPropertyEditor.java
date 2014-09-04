package controllers.dataproperty.impl;

import controllers.dataproperty.DataPropertyEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.datatypes.TypedValue;
import models.ontology.CoraDataPropertyModel;
import models.ontology.CoraInstanceModel;

import java.util.Set;

/**
 * Created by daniel on 04.09.14.
 */
public class GenericDataPropertyEditor implements DataPropertyEditor {

    @FXML
    private Label lblInstanceName;

    @FXML
    private Label lblDatatypeName;

    @FXML
    private TextField txtValue;

    @FXML
    private Button btnSave;

    @FXML
    private ComboBox<CoraDataPropertyModel> comboProperty;

    private Stage stage;
    private CoraInstanceModel instance;
    private CoraDataPropertyModel property;
    private TypedValue<?> typedValue;
    private Class<? extends TypedValue> dataType;

    @FXML
    private void initialize() {
        comboProperty.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CoraDataPropertyModel>() {
            @Override
            public void changed(ObservableValue<? extends CoraDataPropertyModel> observableValue,
                                CoraDataPropertyModel oldValue,
                                CoraDataPropertyModel newValue) {
                onChangePropertySelection(oldValue, newValue);
            }
        });
    }

    @FXML
    private void onSave() {
        String strValue = txtValue.getText();
        if(dataType == null) {
            System.err.println("Kein Datentyp");
            return;
        }

        TypedValue typedValue;
        try {
            typedValue = dataType.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        if(!typedValue.isValidString(strValue)) {
            System.err.println("Fehler: Stringformat ungültig!");
            return;
        }

        typedValue.setFromString(strValue);

        CoraDataPropertyModel property = comboProperty.getSelectionModel().getSelectedItem();
        //instance.createDataPropertyRelation(property, typedValue);
        instance.createDataPropertyAssertion(property, typedValue);

        if(stage != null) {
            stage.close();
        }
    }

    @FXML
    private void onCancel() {
        if(stage != null) {
            stage.close();
        }
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setSubjectAndPredicat(CoraInstanceModel instance, CoraDataPropertyModel property) {
        this.instance = instance;
        lblInstanceName.setText(instance.toString());

        if(property != null) {
            this.property = property;

            lblDatatypeName.setText(dataType.getSimpleName());

            ObservableList<CoraDataPropertyModel> dataProperties = FXCollections.observableArrayList();
            dataProperties.add(property);
            comboProperty.setItems(dataProperties);
            comboProperty.getSelectionModel().selectFirst();
        } else {
            Set<CoraDataPropertyModel> dpSet = instance.getAvailableDataProperties();
            ObservableList<CoraDataPropertyModel> dataProperties = FXCollections.observableArrayList(dpSet);
            comboProperty.setItems(dataProperties);
            comboProperty.getSelectionModel().selectFirst();
        }
    }

    @Override
    public void setValue(TypedValue value) {
        typedValue = value;
        txtValue.setText(value.getAsString());
    }

    private void onChangePropertySelection(CoraDataPropertyModel oldValue, CoraDataPropertyModel newValue) {
        if(newValue != null) {

            try {
                this.dataType = (Class<? extends TypedValue>) newValue.getRangeDataType();
            } catch (ClassCastException e) {
                this.dataType = null;
            }

            if(this.dataType != null) {
                lblDatatypeName.setText(this.dataType.getSimpleName());
                lblDatatypeName.setTextFill(Color.BLACK);
                btnSave.setDisable(false);
            } else {
                lblDatatypeName.setText("Unbekannter Datentyp");
                lblDatatypeName.setTextFill(Color.RED);
                btnSave.setDisable(true);
            }
        }
    }
}
