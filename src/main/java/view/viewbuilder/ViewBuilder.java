package view.viewbuilder;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mainapp.MainApplication;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by daniel on 23.10.14.
 */
public class ViewBuilder {

    private static ViewBuilder instance;

    /**
     * Singleton
     * @return Instanz der Klasse <code>ViewBuilder</code>
     */
    public static ViewBuilder getInstance() {
        return (instance == null)? (instance = new ViewBuilder()) : instance;
    }

    /**
     * Erzeugt einen neuen FXMLLoader für die View mit dem Dateinamen <code>fxmlFile</code>.
     * Die standard i18n-Datei wird automatisch von <code>MainApplication</code> bezogen.
     * @param fxmlFile Dateiname der View
     * @return der FXMLLoader
     */
    public FXMLLoader createLoader(String fxmlFile) {
        URL fxmlFileLocation = this.getClass().getClassLoader().getResource(fxmlFile);
        ResourceBundle texts = MainApplication.getInstance().getTexts();
        return new FXMLLoader(fxmlFileLocation, texts);
    }

    /**
     * Zeigt die View mit dem Dateinamen <code>fxmlFile</code> als modalen Dialog an.
     * Im neu erzeugten View-Controller wird nach einer mit <code>@InjectStage</code> annotierten methode gesucht,
     * der dann die neu erzeugte Stage übergeben wird
     * @see view.viewbuilder.StageInject
     * @param fxmlFile Dateiname der View (fxml-Datei)
     * @param parentStage Die Stage, zu der dieser Dialog modal sein soll
     * @param <T> Typ des Controllers
     * @return Der neu erzeugte Controller
     */
    public <T> T createModal(String fxmlFile, Stage parentStage) {
        FXMLLoader loader = createLoader(fxmlFile);
        Parent pane;

        try {
            pane = loader.load();
        } catch(IOException e) {
            return null;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

        T controller = loader.getController();

        tryInjectStage(stage, controller);

        return controller;
    }

    /**
     * Zeigt die View mit dem Dateinamen <code>fxmlFile</code> als Dialog an.
     * Im neu erzeugten View-Controller wird nach einer mit <code>@InjectStage</code> annotierten methode gesucht,
     * der dann die neu erzeugte Stage übergeben wird
     * @see view.viewbuilder.StageInject
     * @param fxmlFile Dateiname der View (fxml-Datei)
     * @param stage Die Stage, auf der der Dialog angezeigt werden soll
     * @param <T> Typ des Controllers
     * @return Der neu erzeugte Controller
     */
    public <T> T createDialog(String fxmlFile, Stage stage) {
        FXMLLoader loader = createLoader(fxmlFile);
        Parent pane;

        try {
            pane = loader.load();
        } catch(IOException e) {
            return null;
        }

        if(stage == null) {
            stage = new Stage();
        }

        Scene scene = new Scene(pane);
        stage.setScene(scene);

        stage.show();

        T controller = loader.getController();

        tryInjectStage(stage, controller);

        return controller;
    }

    /**
     * Zeigt die View mit dem Dateinamen <code>fxmlFile</code> als Dialog an. Dazu wird eine neue Stage erzeugt.
     * Im neu erzeugten View-Controller wird nach einer mit <code>@InjectStage</code> annotierten methode gesucht,
     * der dann die neu erzeugte Stage übergeben wird
     * @see view.viewbuilder.StageInject
     * @param fxmlFile Dateiname der View (fxml-Datei)
     * @param <T> Typ des Controllers
     * @return Der neu erzeugte Controller
     */
    public <T> T createDialog(String fxmlFile) {
        return createDialog(fxmlFile, null);
    }

    /**
     * Sucht im Object <code>object</code> nach einer Annotation <code>StageInject</code> und versucht die über
     * gebene Stage zu injezieren
     * @param stage Die Stage, die injeziert werden soll
     * @param object Das Objekt, in das die Stage injeziert werden soll (Üblicherweise ein FXML-Controller)
     * @param <T> Typ des Objekts (Üblicherweise ein FXML-Controller)
     */
    private <T> void tryInjectStage(Stage stage, T object) {
        Class c = object.getClass();
        while(c != Object.class) {
            Method[] methods = c.getMethods();
            for(Method m : methods) {
                if(m.isAnnotationPresent(StageInject.class)) {
                    System.out.println("Injectable: " + m.getName());
                    try {
                        m.invoke(object, stage);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    return;
                }
            }
            c = c.getSuperclass();
        }
    }

    /**
     * Gibt einen Sprach-String zurück.
     * @param key Der Schlüssel
     * @return Der String in der Standardsprache
     */
    public String getText(String key) {
        return MainApplication.getInstance().getTexts().getString(key);
    }
}
