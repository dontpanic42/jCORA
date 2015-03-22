package controllers;

import controllers.commons.ThrowableErrorViewController;
import controllers.commons.WaitViewController;
import controllers.queryeditor.QueryViewController;
import controllers.retrieval.RetrievalResultsViewController;
import controllers.settings.SettingsViewController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mainapp.MainApplication;
import models.cbr.*;
import view.Commons;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by daniel on 24.08.14.
 */
public class MainAppViewController implements CoraCaseBase.CaseBaseChangeHandler {

    private static final String CASE_VIEW_VIEW_FILE = "views/caseView.fxml";
    private static final String QUERY_VIEW_VIEW_FILE = "views/queryeditor/queryView.fxml";
    private static final String RETRIEVAL_RESULTS_VIEW_FILE = "views/retrieval/retrievalResultsView.fxml";
    private static final String SPARQL_EDITOR_VIEW_FILE = "views/cbquery/cbQueryEditor.fxml";

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane caseBaseTabPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private CaseBaseViewController caseBaseViewController;
    private SimpleObjectProperty<CoraCaseBase> caseBase = new SimpleObjectProperty<>();

    private HashMap<String, Tab> openCases = new HashMap<String, Tab>();

    @FXML
    public void initialize() throws IOException {
        menuBar.setUseSystemMenuBar(true);
        initializeCaseBaseView();
    }

    private void initializeCaseBaseView() {
        caseBaseViewController.caseBaseProperty().bind(caseBaseProperty());
    }

    @FXML
    private void onShowSettings() {
        SettingsViewController.showSettings();
    }

    @SuppressWarnings("unused")
    @FXML
    private void onNewQuery() throws Throwable {
        FXMLLoader loader = ViewBuilder.getInstance().createLoader(QUERY_VIEW_VIEW_FILE);
        AnchorPane pane = loader.load();

        Stage stage = new Stage();
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setTitle(ViewBuilder.getInstance().getText("ui.query_view.title"));
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

    @FXML
    public void showSPARQLEditor() throws IOException {
        FXMLLoader loader = ViewBuilder.getInstance().createLoader(SPARQL_EDITOR_VIEW_FILE);
        AnchorPane pane = loader.load();

        Stage stage = new Stage();
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setTitle(ViewBuilder.getInstance().getText("ui.cbquery.editor_title"));
        stage.show();
    }

    /**
     * Zeigt einen Fall anhand dessen FallId. Die Methode merkt sich die derzeit geöffneten Fälle. Ist ein
     * Fall bereits geöffnet, so wird dem entsprechenden Tab der Fokus gegeben. Es wird jedoch verhindert,
     * das ein Fall mehrfach geöffnet wird, da es sonst zu synchronisationsproblemen kommen kann.
     * @see controllers.MainAppViewController#showCase(models.cbr.CoraCaseModel, String)
     * @param caseID Die Fall-Id
     * @throws IOException
     */
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
                        Commons.showException(e);
                    }
                } else if(newState == Worker.State.FAILED) {
                    waitView.close();
                    loadCaseTask.getException().printStackTrace();
                    Commons.showException(loadCaseTask.getException());
                }

            });

            new Thread(loadCaseTask).start();
        }
    }

    /**
     * Zeigt einen In-Memory Fall an.
     * Achtung: Im Gegensatz zu <code>showCase(String)</code> überprüft diese Methode nicht, ob der Fall bereits
     * geöffnet ist! Synchronisation ist also Aufgabe der aufrufenden Methode!
     * @see controllers.MainAppViewController#showCase(String)
     * @param caseModel Das Fallmodell
     * @param tabName Label des zu öffnenden Strings
     */
    public void showCase(final CoraCaseModel caseModel, String tabName) {
        try {
            Tab caseTab = new Tab();
            caseTab.setText(tabName);
            AnchorPane casePane = new AnchorPane();
            caseTab.setContent(casePane);

            FXMLLoader loader = ViewBuilder.getInstance().createLoader(CASE_VIEW_VIEW_FILE);
            AnchorPane viewPane = loader.load();

            casePane.getChildren().add(viewPane);
            tabPane.getTabs().add(caseTab);
            tabPane.getSelectionModel().select(caseTab);

            CaseViewController c = loader.getController();
            c.showInstance(caseModel.getCaseRoot());

            final Stage parentStage = MainApplication.getInstance().getMainStage();
            c.setStage(parentStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CaseViewController createCaseView(String caseID) throws IOException {
        Tab caseTab = new Tab();
        ImageView caseIcon = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("icons/case.png")));
        caseIcon.setFitWidth(24.0);
        caseIcon.setFitHeight(24.0);
        caseTab.setGraphic(caseIcon);

        caseTab.setText(caseID);
        AnchorPane casePane = new AnchorPane();
        caseTab.setContent(casePane);

        FXMLLoader loader = ViewBuilder.getInstance().createLoader(CASE_VIEW_VIEW_FILE);
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

    public void showRetrievalResults(CoraQueryModel query, List<CoraRetrievalResult> results) throws IOException {
        FXMLLoader loader = ViewBuilder.getInstance().createLoader(RETRIEVAL_RESULTS_VIEW_FILE);
        AnchorPane pane = loader.load();

        Tab caseTab = new Tab();

        ImageView caseIcon = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("icons/retrieval-result.png")));
        caseIcon.setFitWidth(24.0);
        caseIcon.setFitHeight(24.0);
        caseTab.setGraphic(caseIcon);

        caseTab.setText(ViewBuilder.getInstance().getText("ui.retrieval_results.tab_title"));
        AnchorPane casePane = new AnchorPane();
        caseTab.setContent(casePane);

        casePane.getChildren().add(pane);
        tabPane.getTabs().add(caseTab);
        tabPane.getSelectionModel().select(caseTab);

        RetrievalResultsViewController controller = loader.getController();
        controller.setRetrievalResults(results);
        controller.setQuery(query);
    }

    @SuppressWarnings("unused")
    @FXML
    private void onExit() {
        MainApplication.getInstance().exitApp();
    }
}
