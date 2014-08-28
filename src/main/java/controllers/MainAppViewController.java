package controllers;

import controllers.commons.WaitViewController;
import controllers.queryeditor.QueryViewController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import view.Commons;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by daniel on 24.08.14.
 */
public class MainAppViewController implements CoraCaseBase.CaseBaseChangeHandler {

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane caseBaseTabPane;

    private CaseBaseViewController caseBaseViewController;

    private HashMap<String, Tab> openCases = new HashMap<String, Tab>();

    @FXML
    public void initialize() throws IOException {
        initializeCaseBaseView();
    }

    private void initializeCaseBaseView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getClassLoader().getResource("views/caseBaseView.fxml"));
        AnchorPane pane = loader.load();

        caseBaseTabPane.getChildren().setAll(pane);
        caseBaseViewController = loader.getController();
    }

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

    public void setCaseBase(CoraCaseBase caseBase) {
        caseBaseViewController.setCaseBase(caseBase);
    }

    public void showCase(final String caseID) throws IOException {
        if(openCases.containsKey(caseID)) {
            tabPane.getSelectionModel().select(openCases.get(caseID));
        } else {
            final CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();

            /*
            Das folgende Konstrukt ist eine Threaded-Version von

            // In einem neuen Thread
            CoraCaseModel c = caseBase.loadCase(caseID);
            // Im javafx-Thread
            CaseViewController controller = createCaseView(caseID);
            controller.showInstance(c.getCaseRoot());
             */

            final Stage parentStage = MainApplication.getInstance().getMainStage();
            final WaitViewController waitView = Commons.createWaitScreen(parentStage);

            (new Thread(() -> {
                    try {
                        final CoraCaseModel c = caseBase.loadCase(caseID);
                        //switch to javafx-thread to show the created case...
                        Platform.runLater(() -> {
                            try {
                                CaseViewController controller = createCaseView(caseID);
                                controller.showInstance(c.getCaseRoot());
                                controller.setStage(parentStage);

                                waitView.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        return;
                    }
                })
            ).start();
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
}
