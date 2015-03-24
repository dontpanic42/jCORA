package controllers.commons;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller für den "Bitte Warten"-Dialog
 *
 * Created by daniel on 26.08.14.
 */
public class WaitViewController {
    /**
     * Label für den "Bitte Warten"-Text
     */
    @FXML
    private Label lblText;
    /**
     * Stage, auf der dieser Dialog angezeigt wird
     */
    private Stage stage;
    /**
     * Setter für die Stage
     * @param stage Stage, auf der dieser Dialog angezeigt wird
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    /**
     * Setzt den Text für das <code>lblText</code>-Label
     * @param text
     */
    public void setText(String text) {
        lblText.setText(text);
    }
    /**
     * Schließt den Dialog
     */
    public void close() {
        if(stage != null) {
            stage.close();
        }
    }
}
