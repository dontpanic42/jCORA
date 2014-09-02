package controllers.retrieval;

import com.sun.glass.ui.Application;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mainapp.MainApplication;
import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraRetrievalResult;
import services.retrieval.KNNRetrievalService;
import services.retrieval.RetrievalProgressHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by daniel on 31.08.14.
 *
 * Der Chart-Code orientiert sich an der ersten Antwort aus dem folgenden
 * Stackoverflow-Thread, gegeben von Benutzer "Peter Penzov"
 * http://stackoverflow.com/questions/22089022/line-chart-live-update
 */
public class RetrievalViewController implements RetrievalProgressHandler {

    private static final int MAX_CHART_VALUES = 10;

    @FXML
    private AnchorPane anchorPaneChart;

    @FXML
    private Label lblStatus;

    @FXML
    private ProgressIndicator spinner;

    @FXML
    private Button btnShowResults;

    private Stage stage;

    private NumberAxis yAxis;
    private NumberAxis xAxis;
    private XYChart.Series series = new XYChart.Series<Number, Number>();
    private ConcurrentLinkedQueue<Number> dataQueue = new ConcurrentLinkedQueue<Number>();
    private int xSeriesCounter = 0;

    @FXML
    private void initialize() {
        xAxis = new NumberAxis(0,MAX_CHART_VALUES,MAX_CHART_VALUES/10);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);
        xAxis.setAnimated(true);

        yAxis = new NumberAxis(0.0, 1.0, 0.1);
        yAxis.setAutoRanging(false);
        yAxis.setAnimated(false);

        final LineChart<Number, Number> sc = new LineChart<Number, Number>(xAxis, yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {

            }
        };

        anchorPaneChart.getChildren().add(sc);
        anchorPaneChart.setLeftAnchor(sc, 0.0);
        anchorPaneChart.setTopAnchor(sc, 0.0);
        anchorPaneChart.setRightAnchor(sc, 0.0);
        anchorPaneChart.setBottomAnchor(sc, 0.0);

        sc.getData().add(series);
        sc.setLegendVisible(false);
        sc.setAnimated(true);

        //Aktualisiere das Chart jeden Frame
        new AnimationTimer() {
            @Override public void handle(long now) {
                updateChartValues();
            }
        }.start();
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Aktualisiert das Chart, wenn neue Daten im queue vorliegen.
     */
    private void updateChartValues() {
        if(dataQueue.isEmpty()) {
            return;
        }

        series.getData().add(new XYChart.Data<Number, Number>(xSeriesCounter++, dataQueue.remove()));

        //Entfernen, wenn zu viele Punkte vorhanden
        if (series.getData().size() > MAX_CHART_VALUES) {
            series.getData().remove(0, series.getData().size() - MAX_CHART_VALUES);
        }

        //Verschieben
        xAxis.setLowerBound(xSeriesCounter-MAX_CHART_VALUES);
        xAxis.setUpperBound(xSeriesCounter-1);
    }

    public void startRetrieval(CoraQueryModel query) {
        btnShowResults.setDisable(true);

        KNNRetrievalService retrievalService = new KNNRetrievalService();
        retrievalService.setQuery(query);
        retrievalService.setCaseBase(MainApplication.getInstance().getCaseBase());
        retrievalService.addProgressHandler(this);

        (new Thread(retrievalService)).start();
    }


    @Override
    public void onProgress(int max, int current, CoraQueryModel query, CoraCaseModel lastCase, Float lastSim, CoraCaseModel nextCase) {
        if(lastSim != null) {
            dataQueue.add(lastSim);
        }

        Application.invokeLater(() -> {
            if(nextCase != null) {
                lblStatus.setText("Berechne Ähnlichkeit für: " + nextCase.getCaseId());
            }
        });
    }

    @Override
    public void onFinish(CoraQueryModel query, List<CoraRetrievalResult> results) {
        Application.invokeLater(() -> {
            btnShowResults.setOnAction((ActionEvent e) -> {
                try {
                    if(getStage() != null) {
                        getStage().close();
                    }

                    MainApplication.getInstance().getMainAppView().showRetrievalResults(results);
                    MainApplication.getInstance().getMainStage().requestFocus();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            btnShowResults.setDisable(false);

            spinner.setVisible(false);
            lblStatus.setText("Fertig.");
        });
    }

    @Override
    public void onError(CoraQueryModel query, Throwable error) {
        Application.invokeLater(() -> {
            lblStatus.setText("Fehler: " + error.getMessage());
            error.printStackTrace();
        });
    }
}
