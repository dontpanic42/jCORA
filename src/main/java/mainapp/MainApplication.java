package mainapp;

import controllers.MainAppViewController;
import controllers.settings.SettingsViewController;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
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
 * Hauptklasse der Anwendung.
 *
 * Zeigt beim ersten Start oder bei einem Update den "Einstellungen"-Dialog an.
 * Daraufhin wird die Fallbasis geladen und das Hauptfenster wird angezeigt.
 * Diese Klasse enthält zudem einige anwendungsweit benötigte Referenzen wie die Sprache,
 * die Fallbasis und das Hauptfenster.
 *
 * Created by daniel on 24.08.14.
 */
public class MainApplication extends Application {
    /**
     * Name der Anwendung als String
     */
    public static final String APPLICATION_NAME = "jCora";
    /**
     * Version der Anwendung als String. Wird die Version geändert, wird der Nutzer vor
     * Anwendungsstart aufgefordert, die Einstellungen zu überprüfen
     */
    public static final String VERSION_STRING = "v1.0";
    /**
     * Standardsprache, falls keine kompatible Sprach ermittelt werden konnte oder die
     * Sprache (noch) nicht konfiguriert ist.
     */
    public static final Language DEFAULT_LANGUAGE = Language.ENGLISH;
    /**
     * Die aktuelle Sprache der Anwendung
     */
    private Language currentLanguage = DEFAULT_LANGUAGE;
    /**
     * FXML-Datei für das Hauptfenster
     */
    private static final String MAINAPP_VIEW_FILE = "views/mainAppView.fxml";
    /**
     * Einstellungen dieser Klasse. Ist die Einstellung "AppIsConfigured"+VERSION_STRING nicht vorhanden,
     * wird vor dem Anwendungsstart (d.h. vor dem Laden der Fallbasis etc.) der Einstellungen
     * Dialog angzeigt.
     */
    private final Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);
    /**
     * Singleton-Instanz dieser Klasse
     */
    private static MainApplication instance;
    /**
     * Singleton-Konstruktor. Gibt eine Instanz dieser Klasse zurück.
     * @return Singleton-Instanz
     */
    public static MainApplication getInstance() {
        return instance;
    }
    /**
     * Controller des Hauptfensters
     */
    private MainAppViewController mainAppViewController;
    /**
     * Die Stage, die das Hauptfenster der Anwendung repräsentiert
     */
    private Stage mainStage;
    /**
     * Property, das die Fallbasis enthält
     */
    private SimpleObjectProperty<CoraCaseBase> caseBase = new SimpleObjectProperty<>();
    /**
     * Asynchroner <code>Task</code> für das Laden der Fallbasis
     */
    private Task<CoraCaseBase> initCaseBaseTask;
    /**
     * Das <code>ResourceBundle</code>, das die Übersetzungen für die Anwendungsoberflächen enthält
     */
    private ResourceBundle texts;

    /**
     * Gibt das Resource-Bundle zurück, in dem die Übersetzungen als *.properties Datei(en)
     * vorhanden sind. Die Properties-Dateien sind im <code>Languages</code>-enum definiert.
     * @see models.Language
     * @return Das <code>ResourceBundle</code>, das die Übersetzungen für die Anwendungsoberflächen enthält
     */
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

    /**
     * Gibt die aktuelle Sprache der Anwendung zurück. Die Sprache wird jeweils als ISO-Tag
     * ("de", "en" etc.) zurückgegeben.
     * @return Die aktuelle Sprache der Anwendung als ISO-Tag
     */
    public String getLanguage() {
        return currentLanguage.getTag();
    }

    /**
     * Main-Methode der Anwendung. Wird von JavaFX aufgerufen.
     * @param stage Die initiale Stage
     * @throws Exception Exceptions, die beim initialisieren der Anwendung auftreten
     */
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
     * Beendet die Anwendung. Führt den ShutdownHook Thread aus.
     * Sollte aufgerufen werden um die Anwendung zu beenden, da bei einem
     * direkten Aufruf von System.exit ggf. die Fallbasis nicht geschlossen
     * wird und es zu Datenverlust oder Korruption der Fallbasis kommen kann.
     * @see MainApplication#installShutdownHooks()
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
        String basePath;
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

    /**
     * Gibt die Stage zurück, die das Hauptfenster der Anwendung repräsentiert.
     * Die Anwendung hat immer nur ein Hauptfenster (in dem u.a. die Fallbasis angezeigt
     * wird). Wird das Hauptfenster geschlossen, wird die Anwendung beendet.
     * @return Die Stage, die das Hauptfenster der Anwendung repräsentiert
     */
    public Stage getMainStage() {
        return mainStage;
    }

    /**
     * Gibt die Fallbasis der Anwendung zurück. Die Anwendung hat jeweils nur eine Fallbasis. Soll
     * die Fallbasis geändert werden, muss die Anwendung neu gestartet werden.
     * @return
     */
    public CoraCaseBase getCaseBase() {
        return caseBase.get();
    }

    /**
     * Case-base Property
     * @see MainApplication#getCaseBase()
     * @return Das Case-base Property
     */
    @SuppressWarnings("unused")
    private SimpleObjectProperty<CoraCaseBase> caseBaseProperty() {
        return caseBase;
    }

    /**
     * Setter für die Case-base
     * @see MainApplication#getCaseBase()
     * @param caseBase Die Case-base für die Anwendung
     */
    @SuppressWarnings("unused")
    private void setCaseBase(CoraCaseBase caseBase) {
        this.caseBase.set(caseBase);
    }

    /**
     * Gibt den Controller des Hauptfensters zurück. Es existiert immer nur ein Hauptfenster.
     * Wird das Hauptfenster geschlossen, wird die Anwendung beendet.
     * @see controllers.MainAppViewController
     * @return Der Controller des Hauptfensters
     */
    public MainAppViewController getMainAppView() {
        return mainAppViewController;
    }

    /**
     * Main methode. Startet die Anwendungs über die JavaFX <code>launch</code> methode.
     * @see Application#launch(String...)
     * @param args Kommandozeilenparameter als String-Array
     */
    public static void main(String[] args) {
        launch(args);
    }
}
