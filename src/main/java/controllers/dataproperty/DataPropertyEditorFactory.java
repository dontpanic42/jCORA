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
import view.Commons;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;

/**
 * Created by daniel on 04.09.14.
 */
public class DataPropertyEditorFactory {

    private static final String DATAPROPERTY_EDITOR_FILE = "views/dataproperty/editDPGeneric.fxml";

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
        if(editor == null) {
            return;
        }

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
        try {
            FXMLLoader loader = ViewBuilder.getInstance().createLoader(DATAPROPERTY_EDITOR_FILE);
            AnchorPane pane = loader.load();

            GenericDataPropertyEditor c = loader.getController();
            c.setStage(thisStage);
            c.setSubjectAndPredicat(instance, property);

            if(value != null) {
                c.setValue(value);
            }

            return new Scene(pane);
        } catch (IOException e) {
            Commons.showFatalException(e);
            return null;
        }
    }
}
