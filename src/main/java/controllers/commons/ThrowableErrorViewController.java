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
 * Created by daniel on 23.10.14.
 */
public class ThrowableErrorViewController {

    private static final String THROWABLE_ERROR_VIEW_FILE = "views/commons/throwableErrorView.fxml";

    @FXML
    private Button btnIgnore;

    @FXML
    private Label lblDescription;

    @FXML
    private TextArea txtDetails;

    private BooleanProperty canIgnore = new SimpleBooleanProperty(true);
    private ObjectProperty<Throwable> throwable = new SimpleObjectProperty<>();

    public static void showError(Throwable t, boolean canIgnore) {
        FXMLLoader loader = ViewBuilder.getInstance().createLoader(THROWABLE_ERROR_VIEW_FILE);

        try {
            AnchorPane p = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(p));

            ThrowableErrorViewController c = loader.getController();
            c.setCanIgnore(canIgnore);
            c.setThrowable(t);

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Errorview not available: " + THROWABLE_ERROR_VIEW_FILE);
            System.exit(-1);
        }
    }

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
            if(newValue == null) {
                return;
            }

            lblDescription.setText(getDescription(newValue));
            txtDetails.setText(getStackTrace(newValue));
        });
    }

    private String getDescription(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getSimpleName());
        sb.append(" - ");
        sb.append(t.getMessage());

        return sb.toString();
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);

        return sw.toString();
    }

    @SuppressWarnings("unused")
    @FXML
    private void onIgnore() {

    }

    @SuppressWarnings("unused")
    @FXML
    private void onQuitApplication() {
        MainApplication mainApp = MainApplication.getInstance();
        if(mainApp != null) {
            mainApp.exitApp();
        } else {
            System.exit(0);
        }
    }

    public boolean getCanIgnore() {
        return canIgnore.get();
    }

    public BooleanProperty canIgnoreProperty() {
        return canIgnore;
    }

    public void setCanIgnore(boolean canIgnore) {
        this.canIgnore.set(canIgnore);
    }

    public Throwable getThrowable() {
        return throwable.get();
    }

    public ObjectProperty<Throwable> throwableProperty() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable.set(throwable);
    }
}
