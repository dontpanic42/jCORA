package controllers.commons;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mainapp.MainApplication;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Generischer Dialog, der ein <code>Throwable</code> anzeigt. Der Dialog ist grundsätzlich blockierend.
 *
 * Created by daniel on 23.10.14.
 */
public class ThrowableErrorViewController {

    /**
     * Speicherort der FXML-Datei für diesen Dialog
     */
    private static final String THROWABLE_ERROR_VIEW_FILE = "views/commons/throwableErrorView.fxml";
    /**
     * "Ignorieren"-Button: Schließt den Dialog
     */
    @FXML
    private Button btnIgnore;
    /**
     * Kurze beschreibung des Fehlers, besteht aus dem Namen des Throwables und der Message
     * @see Throwable#getClass()
     * @see Throwable#getMessage()
     */
    @FXML
    private Label lblDescription;
    /**
     * Zeigt den Stacktrace an
     */
    @FXML
    private TextArea txtDetails;
    /**
     * Wenn <code>canIgnore</code> den Wert <code>true</code> hat, kann der Fehler ignoriert werden.
     * Andernfalls wird der "ignore"-Button nicht angezeigt und die Anwendung muss beendet werden
     */
    private BooleanProperty canIgnore = new SimpleBooleanProperty(true);
    /**
     * Das Throwable, das angzeigt wird
     */
    private ObjectProperty<Throwable> throwable = new SimpleObjectProperty<>();
    /**
     * Die Stage, auf der dieser Dialog angezeigt wird
     */
    private Stage stage;

    /**
     * Statische, blockierende Methode, die den ThrowableErrorView-Dialog anzeigt
     * @param t Das Throwable, das angezeigt werden soll
     * @param canIgnore Wenn <code>true</code> wird der "ignorieren"-Button ausgeblendet und die Anwendung bei schließen des
     *                  Dialogs beendet
     */
    public static void showError(Throwable t, boolean canIgnore) {
        FXMLLoader loader = ViewBuilder.getInstance().createLoader(THROWABLE_ERROR_VIEW_FILE);

        try {
            AnchorPane p = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(p));

            ThrowableErrorViewController c = loader.getController();
            c.setCanIgnore(canIgnore);
            c.setThrowable(t);
            c.setStage(stage);

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Errorview not available: " + THROWABLE_ERROR_VIEW_FILE);
            System.exit(-1);
        }
    }

    /**
     * Setzt die Stage für diesen Dialog. Wenn der Fehler nicht ignoriert werden kann,
     * wirdd die gesamte Anwendung mit schließen des Dialogs beendet.
     * @param stage Die Stage, in der dieser Dialog angezeigt wird
     */
    private void setStage(Stage stage) {
        this.stage = stage;
        this.stage.onCloseRequestProperty().addListener((windowEvent) -> {
            if(!canIgnore.getValue()) {
                onQuitApplication();
            }
        });
    }

    /**
     * Initialisiert den Dialog
     */
    @SuppressWarnings("unused")
    @FXML
    private void initialize() {
        canIgnore.addListener((ov, oldValue, newValue) -> {
            if(oldValue && !newValue) {
                btnIgnore.setVisible(false);
            } else if(!oldValue && newValue) {
                btnIgnore.setVisible(true);
            }
        });

        throwable.addListener((ov, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            lblDescription.setText(getDescription(newValue));
            txtDetails.setText(getStackTrace(newValue));
        });
    }

    /**
     * Gibt die Beschreibung des Fehlers als <code>String</code> zurück.
     * Die Beschreibung besteht aus dem Namen des Klasse des Throwables
     * (also z.B. <code>NullPointerException</code>) und der Message
     * @param t Das Throwable, dessen Beschreibung gesucht wird
     * @return Die Beschreibung des Throwables
     */
    private String getDescription(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getSimpleName());
        sb.append(" - ");
        sb.append(t.getMessage());

        return sb.toString();
    }

    /**
     * Gibt den StackTrace als Multiline-String zurück
     * @param t Das Throwable, dessen StackTrace benötigt wird
     * @return Der StackTrace als String
     */
    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);

        return sw.toString();
    }

    /**
     * Wird aufgerufen, wenn der Nutzer den "ignorieren"-Button klickt. Schließt den
     * Dialog und setzt die Ausführung der Anwendung fort.
     */
    @SuppressWarnings("unused")
    @FXML
    private void onIgnore() {
        stage.close();
    }

    /**
     * Beendet die Anwendung. Wird durch einen Klick auf "jCORA Beenden" oder durch das
     * schließen des Dialogs aufgerufen (letzteres nur, wenn <code>canIgnore</code> gleich
     * <code>false</code> ist)
     */
    @SuppressWarnings("unused")
    @FXML
    private void onQuitApplication() {
        /* Möglicherweise wird dieser Dialog angezeigt, befor die Anwendung
         * vollständig initialisiert wurde - in diesem Fall wird einfach
         * System#exit() aufgerufen... */
        MainApplication mainApp = MainApplication.getInstance();
        if(mainApp != null) {
            mainApp.exitApp();
        } else {
            System.exit(0);
        }
    }

    /**
     * Getter für das <code>canIgnore</code> Property.
     * @return Wert des <code>canIgnore</code>-Properties.
     */
    public boolean getCanIgnore() {
        return canIgnore.get();
    }

    /**
     * <code>canIgnore</code>-Property
     * @see ThrowableErrorViewController#getCanIgnore()
     * @see ThrowableErrorViewController#setCanIgnore(boolean)
     * @return <code>canIgnore</code>-Property
     */
    @SuppressWarnings("unused")
    public BooleanProperty canIgnoreProperty() {
        return canIgnore;
    }

    /**
     * Setter für das <code>canIgnore</code> Property.
     * @param canIgnore Wert des <code>canIgnore</code>-Properties.
     */
    public void setCanIgnore(boolean canIgnore) {
        this.canIgnore.set(canIgnore);
    }

    /**
     * Getter für das <code>throwable</code> Property
     * @return Das Throwable, das derzeit angezeigt wird.
     */
    public Throwable getThrowable() {
        return throwable.get();
    }

    /**
     * <code>throwable</code>-Property
     * @see ThrowableErrorViewController#setThrowable(Throwable)
     * @see ThrowableErrorViewController#getThrowable()
     * @return <code>throwable</code>-Property
     */
    @SuppressWarnings("unused")
    public ObjectProperty<Throwable> throwableProperty() {
        return throwable;
    }

    /**
     * Setter für das <code>throwable</code> Property
     * @param throwable Das Throwable, das angezeigt werden soll.
     */
    public void setThrowable(Throwable throwable) {
        this.throwable.set(throwable);
    }
}
