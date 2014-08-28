package controllers.queryeditor;

import controllers.CaseViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.cbr.CoraCaseModel;

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
    private void onStartRetrieval() {

    }

    public void setCase(CoraCaseModel caseModel) {
        this.caseModel = caseModel;
        caseViewController.showInstance(caseModel.getCaseDescription());
    }
}
