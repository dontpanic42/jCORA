package controllers.commons;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Created by daniel on 26.08.14.
 */
public class WaitViewController {

    @FXML
    private Label lblText;

    private Stage stage;

    @FXML
    private void initialize() {

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setText(String text) {
        lblText.setText(text);
    }

    public void close() {
        if(stage != null) {
            stage.close();
        }
    }
}
