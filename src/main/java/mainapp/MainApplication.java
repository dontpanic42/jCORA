package mainapp;

import controllers.MainAppViewController;
import controllers.commons.ThrowableErrorViewController;
import controllers.settings.SettingsViewController;
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
import view.Commons;
import view.viewbuilder.ViewBuilder;

import java.io.*;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Created by daniel on 24.08.14.
 */
public class MainApplication extends Application {

    public static final String APPLICATION_NAME = "jCora";
    public static final String VERSION_STRING = "v1.0";

    private Language currentLanguage = Language.GERMAN;
    public static final Language DEFAULT_LANGUAGE = Language.ENGLISH;

    private static final String MAINAPP_VIEW_FILE = "views/mainAppView.fxml";
    private final Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);

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
        System.out.println("Java version: " + System.getProperty("java.version"));

        setLanguage();

        //Singleton-Instanz
        instance = this;

        // Wenn die Anwendung zum ersten mal gestartet wird (oder ein Update eingespielt wurde), zeige den
        // Einstellungsdialog an. (Benutze <code>while</code> damit der user nicht einfach "Abbrechen" klickt...)
        while(!prefs.getBoolean("AppIsConfigured" + VERSION_STRING, false)) {
            SettingsViewController.showSettings(true, false);
        }

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
        // Wenn vom nutzer über die Einstellungen eine Sprache vorgegeben wurde, nutze diese
        if(prefs.get("language", null) != null) {
            Language userLang = Language.getLanguageByTag(prefs.get("language", null));
            currentLanguage = userLang;
            return;
        }

        // Wenn keine Sprache vorgegeben wurde, benutze die Systemsprache (Falls möglich)
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
     * Gibt das Nutzerverzeichnis ("user.home") zurück, wenn nicht als *.jar ausgeführt oder anderweitig nicht verfügbar.
     * @return Pfad als String MIT folgendem "/"
     */
    public static String getApplicationPath() {
        String basePath = "";
        try {
            String path = MainApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            basePath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Commons.showFatalException(e);
            return null;
        }

        if(!(new File(basePath).exists())) {
            String homeDir = System.getProperty("user.home");
            if(homeDir.endsWith("/") || homeDir.endsWith("\\")) {
                homeDir = homeDir + File.separator;
            }

            return homeDir;
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
                Commons.showFatalException(initCaseBaseTask.getException());
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
