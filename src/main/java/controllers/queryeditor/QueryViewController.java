package controllers.queryeditor;

import controllers.CaseViewController;
import controllers.retrieval.RetrievalViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraWeightModel;

import java.io.IOException;

/**
 * Created by daniel on 26.08.14.
 */
public class QueryViewController {

    @FXML
    private AnchorPane paneCase;

    @FXML
    private AnchorPane paneWeights;

    private CoraCaseModel caseModel;

    private CaseViewController caseViewController;

    private Stage stage;

    @FXML
    private void initialize() throws IOException {
        loadCaseView();
        loadWeightsView();
    }

    private void loadCaseView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getClassLoader().getResource("views/caseView.fxml"));
        AnchorPane pane = loader.load();

        paneCase.getChildren().setAll(pane);
        caseViewController = loader.getController();
        caseViewController.setStage(stage);
    }

    public void setStage(Stage parentStage) {
        this.stage = parentStage;
    }

    private void loadWeightsView() {

    }

    @FXML
    private void onStartRetrieval() throws IOException {
        CoraCaseModel c = this.caseModel;
        CoraWeightModel weights = new CoraWeightModel();
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
        controller.startRetrieval(queryModel);
    }

    public void setCase(CoraCaseModel caseModel) {
        this.caseModel = caseModel;
        caseViewController.showInstance(caseModel.getCaseDescription());
    }
}
