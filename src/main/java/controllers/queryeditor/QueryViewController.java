package controllers.queryeditor;

import controllers.CaseViewController;
import controllers.retrieval.RetrievalResultsViewController;
import controllers.retrieval.RetrievalViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mainapp.MainApplication;
import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraRetrievalResult;
import models.cbr.CoraWeightModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 26.08.14.
 */
public class QueryViewController {

    private CoraCaseModel caseModel;

    @FXML
    private WeightsViewController weightsViewController;

    @FXML
    private CaseViewController caseViewController;

    private Stage stage;

    @SuppressWarnings("unused")
    @FXML
    private void initialize() throws IOException {
        initializeCaseView();
        initializeWeightsView();
    }

    private void initializeCaseView() {
        caseViewController.setStage(stage);
    }

    public void setStage(Stage parentStage) {
        this.stage = parentStage;
    }

    private void initializeWeightsView() {

    }

    @SuppressWarnings("unused")
    @FXML
    private void onStartRetrieval() throws IOException {
        CoraCaseModel c = this.caseModel;
        CoraWeightModel weights = weightsViewController.getWeightModel();//new CoraWeightModel();
        CoraQueryModel queryModel = new CoraQueryModel(c, weights, 10);

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(this.stage);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getClassLoader().getResource("views/retrieval/retrievalView.fxml"));
        AnchorPane pane = loader.load();

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

        RetrievalViewController controller = loader.getController();
        controller.setStage(stage);
        controller.startRetrieval(queryModel);
    }

    public void setCase(CoraCaseModel caseModel) {
        this.caseModel = caseModel;
        caseViewController.showInstance(caseModel.getCaseDescription());
        weightsViewController.setCaseModel(caseModel);
    }
}
