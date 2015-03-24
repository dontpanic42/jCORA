package controllers;

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
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraRetrievalResult;
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

    /**
     * Das Anwendungs-Hauptmenü
     */
    @FXML
    private MenuBar menuBar;

    /**
     * Die Fallbasis-Ansicht
     */
    @FXML
    private CaseBaseViewController caseBaseViewController;
    /**
     * Die Fallbasis, die in der Fallbasis-Ansicht angezeigt wird
     */
    private SimpleObjectProperty<CoraCaseBase> caseBase = new SimpleObjectProperty<>();

    /**
     * Liste der derzeit geöffneten Fälle in der Form (FallID, Tab). Wird dazu genutzt
     * sicherzustellen, das ein Fall jeweils nur in einem Tab geöffnet ist.
     */
    private HashMap<String, Tab> openCases = new HashMap<>();

    @FXML
    public void initialize() throws IOException {
        menuBar.setUseSystemMenuBar(true);
        initializeCaseBaseView();
    }

    /**
     * Initialisiert die Fallbasis-Ansicht
     */
    private void initializeCaseBaseView() {
        caseBaseViewController.caseBaseProperty().bind(caseBaseProperty());
    }

    /**
     * Wird aufgerufen, wenn der "Settings"/"Einstellungen"-Menüpunkt angeclickt wird.
     * Zeigt den "Settings"/"Einstellungen"-Dialog
     */
    @FXML
    private void onShowSettings() {
        SettingsViewController.showSettings();
    }

    /**
     * Erzeugt eine neue CBR-Anfrage
     * @throws Throwable Exceptions, die beim Erstellen der CBR-Anfrage aufgetreten sind
     */
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

    /**
     * Getter für die aktuelle Fallbasis
     * @return Die Fallbasis
     */
    public CoraCaseBase getCaseBase() {
        return caseBase.get();
    }

    /**
     * Getter für das Property, das die akutelle Fallbasis enthält.
     * @return Property, das die akutelle Fallbasis enthält
     */
    public SimpleObjectProperty<CoraCaseBase> caseBaseProperty() {
        return caseBase;
    }

    /**
     * Setter für die aktuelle Fallbasis
     */
    public void setCaseBase(CoraCaseBase caseBase) {
        this.caseBase.set(caseBase);
    }

    /**
     * Zeigt den SPARQL-Query-Editor an. Wird über den Menüpunkt "SPARQL" angezeigt.
     * @throws IOException Wenn die SPARQL-Editor FXML-Datei nicht gefunden wurde
     */
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

    /**
     * Lädt einen Fall mit der ID <code>caseID</code> aus der Fallbasis und zeigt diesen an.
     * @param caseID Die ID des Falls, der geladen werden soll
     * @return Controller der neuen Fallansicht
     * @throws IOException Exception die geworfen wird, wenn die FXML-Datei des Falleditors nicht gefunden wurde
     */
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

    /**
     * Wird aufgerufen, wenn ein Falleditor-Tab geschlossen wird.
     * @param caseId Die ID des Falls, der geschlossen wurde
     */
    private void onCaseTabClose(String caseId) {
        if(openCases.containsKey(caseId)) {
            openCases.remove(caseId);
        }
    }

    @Override
    public void onAddCase(String caseId) {

    }

    /**
     * Wird aufgerufen, wenn ein Fall aus der Fallbasis gelöscht wurde. Schließt ggf. den Tab,
     * in dem der Fall angezeigt wird.
     * @param caseId
     */
    @Override
    public void onRemoveCase(String caseId) {
        if(openCases.containsKey(caseId)) {
            Tab tab = openCases.get(caseId);
            tabPane.getTabs().remove(tab);
            openCases.remove(caseId);
        }
    }

    /**
     * Zeigt die Ergebnisse des CBR-Retrievals in einem neuen Tab
     * @param query Die CBR-Anfrage
     * @param results Die Ergebnisse des CBR-Retrievals
     * @throws IOException Wird geworfen, wenn die FXML-Datei der Ergebniss-Ansicht nicht gefunden wurde
     */
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

    /**
     * Wird aufgerufen, wenn dieses Fenster geschlossen wird. Beendet jCORA.
     */
    @SuppressWarnings("unused")
    @FXML
    private void onExit() {
        MainApplication.getInstance().exitApp();
    }
}
