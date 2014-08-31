package controllers.retrieval;

import com.sun.glass.ui.Application;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import mainapp.MainApplication;
import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraRetrievalResult;
import services.retrieval.KNNRetrievalService;
import services.retrieval.RetrievalProgressHandler;

import java.util.List;

/**
 * Created by daniel on 31.08.14.
 */
public class RetrievalViewController implements RetrievalProgressHandler {

    @FXML
    private Label lblStatus;

    @FXML
    private LineChart<String, Float> chart;

    @FXML
    private void initialize() {

    }

    public void startRetrieval(CoraQueryModel query) {
        KNNRetrievalService retrievalService = new KNNRetrievalService();
        retrievalService.setQuery(query);
        retrievalService.setCaseBase(MainApplication.getInstance().getCaseBase());
        retrievalService.addProgressHandler(this);

        (new Thread(retrievalService)).start();
    }

    private XYChart.Series<String, Float> series = new XYChart.Series<>();

    @Override
    public void onProgress(int max, int current, CoraQueryModel query, CoraCaseModel lastCase, Float lastSim, CoraCaseModel nextCase) {
        Application.invokeLater(() -> {
            if(lastCase != null && lastSim != null) {
                lblStatus.setText("Fall " + lastCase.getCaseId() + ": " + lastSim);

                if(chart.getData().size() == 0) {
                    chart.getData().add(series);
                }

                series.getData().add(new XYChart.Data<>(lastCase.getCaseId(), lastSim));

                if(series.getData().size() > 10) {
                    series.getData().remove(0);
                }

            } else {
                lblStatus.setText("Berechen Ähnlichkeit: " + nextCase.getCaseId());
            }


        });
    }

    @Override
    public void onFinish(CoraQueryModel query, List<CoraRetrievalResult> results) {
        Application.invokeLater(() -> {
            lblStatus.setText("Fertig. " + results.size() + " Fälle gefunden.");
        });

        for(CoraRetrievalResult r : results) {
            System.out.println(r.getCaseId() + ": " + r.getSimilarity());
        }
    }

    @Override
    public void onError(CoraQueryModel query, Throwable error) {
        Application.invokeLater(()-> {
            lblStatus.setText("Bei der Berechnung ist ein Fehler aufgetreten: " + error.getMessage());
        });

        error.printStackTrace();
    }
}
