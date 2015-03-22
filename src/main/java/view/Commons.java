package view;

import controllers.commons.ThrowableErrorViewController;
import controllers.commons.WaitViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;

/**
 * Created by daniel on 26.08.14.
 */
public class Commons {

    private static final String WAIT_VIEW_FILE = "views/commons/waitView.fxml";

    public static WaitViewController createWaitScreen(Stage parent) throws IOException {
        return createWaitScreen(parent, null);
    }

    public static WaitViewController createWaitScreen(Stage parent, String text) throws IOException {
        FXMLLoader loader = ViewBuilder.getInstance().createLoader(WAIT_VIEW_FILE);
        AnchorPane pane = loader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parent);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);

        Scene scene = new Scene(pane);
        stage.setScene(scene);


        WaitViewController c = loader.getController();
        c.setStage(stage);
        if(text != null) {
            c.setText(text);
        }

        stage.show();

        return c;
    }

    public static void showFatalException(Throwable throwable) {
        ThrowableErrorViewController.showError(throwable, false);
    }

    public static void showException(Throwable throwable) {
        ThrowableErrorViewController.showError(throwable, true);
    }
}
