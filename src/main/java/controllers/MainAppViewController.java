package controllers;

import controllers.commons.WaitViewController;
import controllers.queryeditor.QueryViewController;
import controllers.retrieval.RetrievalResultsViewController;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mainapp.MainApplication;
import models.cbr.*;
import view.Commons;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by daniel on 24.08.14.
 */
public class MainAppViewController implements CoraCaseBase.CaseBaseChangeHandler {

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane caseBaseTabPane;

    @FXML
    private MenuBar menuBar;

    private CaseBaseViewController caseBaseViewController;
    private SimpleObjectProperty<CoraCaseBase> caseBase = new SimpleObjectProperty<>();

    private HashMap<String, Tab> openCases = new HashMap<String, Tab>();

    @FXML
    public void initialize() throws IOException {
        menuBar.setUseSystemMenuBar(true);
        initializeCaseBaseView();
    }

    private void initializeCaseBaseView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getClassLoader().getResource("views/caseBaseView.fxml"));
        AnchorPane pane = loader.load();

        caseBaseTabPane.getChildren().setAll(pane);
        caseBaseViewController = loader.getController();
        caseBaseViewController.caseBaseProperty().bind(caseBaseProperty());
    }

    @SuppressWarnings("unused")
    @FXML
    private void onNewQuery() throws Throwable {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getClassLoader().getResource("views/queryeditor/queryView.fxml"));
        AnchorPane pane = loader.load();

        Stage stage = new Stage();
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setTitle("Anfrageeditor: Neue Anfrage");
        stage.show();

        QueryViewController controller = loader.getController();
        CoraCaseModel caseModel = MainApplication.getInstance().getCaseBase().createTemporaryCase();
        controller.setStage(stage);
        controller.setCase(caseModel);
    }

    public CoraCaseBase getCaseBase() {
        return caseBase.get();
    }

    public SimpleObjectProperty<CoraCaseBase> caseBaseProperty() {
        return caseBase;
    }

    public void setCaseBase(CoraCaseBase caseBase) {
        this.caseBase.set(caseBase);
    }

    public void showCase(final String caseID) throws IOException {
        if(openCases.containsKey(caseID)) {
            tabPane.getSelectionModel().select(openCases.get(caseID));
        } else {
            final Stage parentStage = MainApplication.getInstance().getMainStage();
            final WaitViewController waitView = Commons.createWaitScreen(parentStage);

            Task<CoraCaseModel> loadCaseTask = new Task<CoraCaseModel>() {
                @Override
                protected CoraCaseModel call() throws Exception {
                    return caseBaseProperty().getValue().loadCase(caseID);
                }
            };

            loadCaseTask.stateProperty().addListener((ov, oldState, newState) -> {
                if(newState == Worker.State.SUCCEEDED) {
                    try {
                        CaseViewController c = createCaseView(caseID);
                        c.showInstance(loadCaseTask.getValue().getCaseRoot());
                        c.setStage(parentStage);

                        waitView.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            new Thread(loadCaseTask).start();
        }
    }

    private CaseViewController createCaseView(String caseID) throws IOException {
        Tab caseTab = new Tab();
        caseTab.setText(caseID);
        AnchorPane casePane = new AnchorPane();
        caseTab.setContent(casePane);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getClassLoader().getResource("views/caseView.fxml"));
        AnchorPane viewPane = loader.load();

        casePane.getChildren().add(viewPane);
        tabPane.getTabs().add(caseTab);
        tabPane.getSelectionModel().select(caseTab);

        openCases.put(caseID, caseTab);
        caseTab.setOnClosed( (Event e) -> onCaseTabClose(caseID) );

        return loader.getController();
    }

    private void onCaseTabClose(String caseId) {
        if(openCases.containsKey(caseId)) {
            openCases.remove(caseId);
        }
    }

    @Override
    public void onAddCase(String caseId) {

    }

    @Override
    public void onRemoveCase(String caseId) {
        if(openCases.containsKey(caseId)) {
            Tab tab = openCases.get(caseId);
            tabPane.getTabs().remove(tab);
            openCases.remove(caseId);
        }
    }

    public void showRetrievalResults(List<CoraRetrievalResult> results) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getClassLoader().getResource("views/retrieval/retrievalResultsView.fxml"));
        AnchorPane pane = loader.load();

        Tab caseTab = new Tab();
        caseTab.setText("Anfrageergebnisse");
        AnchorPane casePane = new AnchorPane();
        caseTab.setContent(casePane);

        casePane.getChildren().add(pane);
        tabPane.getTabs().add(caseTab);
        tabPane.getSelectionModel().select(caseTab);

        RetrievalResultsViewController controller = loader.getController();
        controller.setRetrievalResults(results);
    }

    @SuppressWarnings("unused")
    @FXML
    private void onExit() {
        MainApplication.getInstance().exitApp();
    }
}
