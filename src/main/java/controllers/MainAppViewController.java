package controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;

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

    public void setCaseBase(CoraCaseBase caseBase) {
        caseBaseViewController.setCaseBase(caseBase);
    }

    public void showCase(String caseID) {
        if(openCases.containsKey(caseID)) {
            tabPane.getSelectionModel().select(openCases.get(caseID));
        } else {
            CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();
            try {

                CoraCaseModel c = caseBase.loadCase(caseID);

                CaseViewController controller = createCaseView(caseID);

                controller.showInstance(c.getCaseRoot());

            } catch (Throwable e) {
                e.printStackTrace();
                return;
            }
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
