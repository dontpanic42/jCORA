package mainapp;

import controllers.MainAppViewController;
import controllers.commons.ThrowableErrorViewController;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.Language;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseBaseImpl;
import view.viewbuilder.ViewBuilder;

import java.io.*;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created by daniel on 24.08.14.
 */
public class MainApplication extends Application {

    public static final String APPLICATION_NAME = "jCora";
    public static final String VERSION_STRING = "v1.0";

    private Language currentLanguage = Language.GERMAN;
    private static final Language DEFAULT_LANGUAGE = Language.ENGLISH;

    private static final String MAINAPP_VIEW_FILE = "views/mainAppView.fxml";

    private static MainApplication instance;
    public static MainApplication getInstance() {
        return instance;
    }

    private MainAppViewController mainAppViewController;

    private Stage mainStage;

    private SimpleObjectProperty<CoraCaseBase> caseBase = new SimpleObjectProperty<>();
    private Task<CoraCaseBase> initCaseBaseTask;
    private ResourceBundle texts;

    public ResourceBundle getTexts() {
        if(texts == null) {
            try {
                final String bundle = currentLanguage.getResourceBundleFileName();
                InputStream is = this.getClass().getClassLoader().getResourceAsStream(bundle);
                InputStreamReader reader = new InputStreamReader(is, "UTF-8");
                texts = new PropertyResourceBundle(reader);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        return texts;
    }

    public String getLanguage() {
        return currentLanguage.getTag();
    }

    @Override
    public void start(Stage stage) throws Exception {
        setLanguage();

        //Singleton-Instanz
        instance = this;
        //Hauptfenster zugänglich machen
        mainStage = stage;
        //Wenn das Hauptfenster geschlossen wird, schließe die Anwendung
        mainStage.setOnCloseRequest((windowEvent) -> exitApp());

        mainStage.setMaximized(true);
        mainStage.setTitle(APPLICATION_NAME + " " + VERSION_STRING);

        mainAppViewController = ViewBuilder.getInstance().createDialog(MAINAPP_VIEW_FILE, stage);
        mainAppViewController.caseBaseProperty().bind(caseBase);

        installShutdownHooks();
        initCaseBase();

    }

    /**
     * Setzt die Anwendungssprache auf die Systemsprache, falls möglich.
     * Ist die Systemsprache nicht als Anwendungssprach verfügbar, setze
     * <code>DEFAULT_LANGUAGE</code> als aktuelle Anwendungssprache.
     */
    private void setLanguage() {
        final String lang = Locale.getDefault().getLanguage();
        Language l = Language.getLanguageByTag(lang);
        if(l == null) {
            currentLanguage = DEFAULT_LANGUAGE;
            System.out.println("i18n: System language '" + Locale.getDefault().getDisplayLanguage() + "' not available.");
            System.out.println("i18n: Setting '" + DEFAULT_LANGUAGE.getName() + "' as default language.");
        } else {
            currentLanguage = l;
        }
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
     * Gibt den Pfad als String zurück, in dem sich die aktuelle *.jar Datei befindet.
     * Gibt null zurück, wenn nicht als *.jar ausgeführt oder anderweitig nicht verfügbar.
     * @return Pfad als String MIT folgendem "/"
     */
    public static String getApplicationPath() {
        String basePath = "";
        try {
            String path = MainApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            basePath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            ThrowableErrorViewController.showError(e, false);
            return null;
        }

        if(!(new File(basePath).exists())) {
            return null;
        } else {
            basePath = new File(basePath).getParent();
            return basePath + File.separator;
        }
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
            } else if(newState == Worker.State.FAILED) {
                ThrowableErrorViewController.showError(initCaseBaseTask.getException(), false);
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
