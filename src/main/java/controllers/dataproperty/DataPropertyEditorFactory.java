package controllers.dataproperty;

import controllers.dataproperty.impl.GenericDataPropertyEditor;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.datatypes.TypedValue;
import models.ontology.CoraDataPropertyModel;
import models.ontology.CoraInstanceModel;

import java.io.IOException;

/**
 * Created by daniel on 04.09.14.
 */
public class DataPropertyEditorFactory {

    /**
     * Zeigt einen zu der DataProperty/Value kombination passenden Editor.
     * (Im moment ist nur der generische Editor vorhanden...)
     * @param instance Die Instanz (Subjekt)
     * @param property Das Dataproperty (Prädikat)
     * @param value Der Wert oder <code>null</code>, wenn das Property neu ist (Objekt)
     */
    public static void showEditor(CoraInstanceModel instance,
                                  CoraDataPropertyModel property,
                                  TypedValue value,
                                  Stage parentStage) {

        Stage stage = new Stage();
        if(parentStage != null) {
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(parentStage);
        }

        Scene editor = createGenericEditor(instance, property, value, stage);

        stage.setScene(editor);
        stage.show();
    }

    /**
     * Erzeugt eine Scene mit dem generischen DataProperty-Editor
     * @param instance Die Instanz (Subjekt)
     * @param property Das Dataproperty (Prädikat)
     * @param value Der Wert oder <code>null</code>, wenn das Property neu ist (Objekt)
     * @param thisStage Die Stage, auf der der Editor angezeigt wird
     * @return Eine Scene mit dem Editor
     */
    private static Scene createGenericEditor(CoraInstanceModel instance,
                                             CoraDataPropertyModel property,
                                             TypedValue value,
                                             Stage thisStage) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(DataPropertyEditorFactory.class
                .getClassLoader()
                .getResource("views/dataproperty/editDPGeneric.fxml"));

        AnchorPane pane;
        try {
            pane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        GenericDataPropertyEditor c = loader.getController();
        c.setStage(thisStage);
        c.setSubjectAndPredicat(instance, property);

        if(value != null) {
            c.setValue(value);
        }

        return new Scene(pane);
    }
}
