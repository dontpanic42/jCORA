package mainapp;

import controllers.MainAppViewController;
import controllers.adaption.AdaptionStackController;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseBaseImpl;
import org.mindswap.pellet.ABox;
import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.PelletOptions;

import javax.naming.ConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by daniel on 24.08.14.
 */
public class MainApplication extends Application {

    public static final String APPLICATION_NAME = "jCora";
    public static final String VERSION_STRING = "Alpha 1";

    private static MainApplication instance;
    public static MainApplication getInstance() {
        return instance;
    }

    private MainAppViewController mainAppViewController;

    private Stage mainStage;

    ///private CoraCaseBase caseBase;
    private SimpleObjectProperty<CoraCaseBase> caseBase = new SimpleObjectProperty<>();
    private Task<CoraCaseBase> initCaseBaseTask;

    @Override
    public void start(Stage stage) throws Exception {
        //Singleton-Instanz
        instance = this;
        //Hauptfenster zugänglich machen
        mainStage = stage;
        //Wenn das Hauptfenster geschlossen wird, schließe die Anwendung
        mainStage.setOnCloseRequest((windowEvent) -> exitApp());

        mainStage.setMaximized(true);
        mainStage.setTitle(APPLICATION_NAME + " " + VERSION_STRING);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getClassLoader().getResource("views/mainAppView.fxml"));
        AnchorPane pane = loader.load();

        mainAppViewController = loader.getController();
        mainAppViewController.caseBaseProperty().bind(caseBase);

        Scene scene = new Scene(pane);
        mainStage.setScene(scene);
        mainStage.show();

        installShutdownHooks();
        initCaseBase();

    }

    /**
     * Installiert einen <code>ShutdownHook</code>, der dafür sorgt,
     * dass die Fallbasis (TDB) geschlossen wird, wenn das Programm z.B. mittels
     * <code>System.exit(0)</code> beendet wird.
     */
    private void installShutdownHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if(caseBase.getValue() != null) {
                    caseBase.getValue().close();
                    System.out.println("CaseBase closed.");
                }
            }
        });
    }


    /**
     * Beendet die Anwendung.
     */
    public void exitApp() {
        System.out.println("Bye.");
        System.exit(0);
    }

    /**
     * Läd die Fallbasis asynchron.
     */
    private void initCaseBase() {
        if(initCaseBaseTask != null) {
            initCaseBaseTask.cancel();
        }

        initCaseBaseTask = new Task<CoraCaseBase>() {
            @Override
            protected CoraCaseBase call() throws Exception {
                return new CoraCaseBaseImpl();
            }
        };

        initCaseBaseTask.stateProperty().addListener((ov, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                caseBase.setValue(initCaseBaseTask.getValue());
            }
        });

        new Thread(initCaseBaseTask).start();
    }

    public Stage getMainStage() {
        return mainStage;
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

    public MainAppViewController getMainAppView() {
        return mainAppViewController;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
