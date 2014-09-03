package mainapp;

import controllers.MainAppViewController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
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

    private CoraCaseBase caseBase;

    @Override
    public void start(Stage stage) throws Exception {
        instance = this;

        mainStage = stage;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                if(caseBase != null) {
                    System.out.println("Bye.");
                    caseBase.close();
                    System.exit(0);
                }
            }
        });
        stage.setMaximized(true);
        stage.setTitle(APPLICATION_NAME + " " + VERSION_STRING);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getClassLoader().getResource("views/mainAppView.fxml"));
        AnchorPane pane = loader.load();

        mainAppViewController = loader.getController();

        initCaseBase();
        mainAppViewController.setCaseBase(caseBase);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    private void initCaseBase() {
        try {
            CoraCaseBase caseBase = new CoraCaseBaseImpl();
            this.caseBase = caseBase;
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public CoraCaseBase getCaseBase() {
        return caseBase;
    }

    public MainAppViewController getMainAppView() {
        return mainAppViewController;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
