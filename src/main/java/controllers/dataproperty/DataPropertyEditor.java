package controllers.dataproperty;

import javafx.stage.Stage;
import models.datatypes.TypedValue;
import models.ontology.CoraDataPropertyModel;
import models.ontology.CoraInstanceModel;

/**
 * Created by daniel on 04.09.14.
 */
public interface DataPropertyEditor {

    public void setStage(Stage stage);

    public void setSubjectAndPredicat(CoraInstanceModel instance, CoraDataPropertyModel property);

    public void setValue(TypedValue value);

}
