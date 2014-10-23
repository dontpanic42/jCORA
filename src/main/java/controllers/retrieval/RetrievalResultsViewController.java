package controllers.retrieval;

import controllers.adaption.AdaptionStackController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraRetrievalResult;
import services.adaption.AdaptionService;
import services.adaption.rules.AdaptionRule;
import view.Commons;
import view.viewbuilder.ViewBuilder;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by daniel on 02.09.14.
 */
public class RetrievalResultsViewController {

    private static final String ADAPTION_STACK_VIEW_FILE = "views/adaption/adaptionStack.fxml";

    @FXML
    private TableView<CoraRetrievalResult> tblResults;

    @FXML
    private TableColumn<CoraRetrievalResult, String> columnCaseId;

    @FXML
    private TableColumn<CoraRetrievalResult, String> columnSimilarity;

    @FXML
    private TableColumn<CoraRetrievalResult, Double> columnSimilarityBar;

    @FXML
    private TableColumn<CoraRetrievalResult, Button> columnAdapt;

    @FXML
    private TableColumn<CoraRetrievalResult, Button> columnView;

    @FXML
    private TextField txtFilter;

    @FXML
    private Accordion accordionLeft;

    private CoraQueryModel query;

    private ObservableList<CoraRetrievalResult> resultsItems;

    @SuppressWarnings("unused")
    @FXML
    private void initialize() {
        final String txtAdapt = ViewBuilder.getInstance().getText("ui.retrieval_results.btn_adapt");
        final String txtShow = ViewBuilder.getInstance().getText("ui.retrieval_results.btn_show");
        final String txtNoResults = ViewBuilder.getInstance().getText("ui.retrieval_results.results_placeholder");

        if(accordionLeft.getPanes().size() > 0) {
            accordionLeft.setExpandedPane(accordionLeft.getPanes().get(0));
        }

        resultsItems = FXCollections.observableArrayList();

        columnCaseId.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CoraRetrievalResult, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<CoraRetrievalResult, String> c) {
                return new ReadOnlyObjectWrapper<>(c.getValue().getCaseId());
            }
        });

        columnSimilarity.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CoraRetrievalResult, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<CoraRetrievalResult, String> c) {
                float val = c.getValue().getSimilarity() * 100;
                Integer intVal = (int) val;
                return new ReadOnlyObjectWrapper<>(intVal.toString() + "%");
            }
        });

        columnSimilarityBar.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CoraRetrievalResult, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<CoraRetrievalResult, Double> c) {
                Double d = (double) c.getValue().getSimilarity();
                return new ReadOnlyObjectWrapper<>(d);
            }
        });

        columnSimilarityBar.setCellFactory(ProgressBarTableCell.forTableColumn());

        columnAdapt.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CoraRetrievalResult, Button>, ObservableValue<Button>>() {
            @Override
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<CoraRetrievalResult, Button> cDataFeatures) {
                Button b = new Button(txtAdapt);

                String selectedCaseId = cDataFeatures.getValue().getCaseId();

                b.setOnAction( (ActionEvent e ) -> onAdapt(selectedCaseId));
                return new ReadOnlyObjectWrapper<>(b);
            }
        });

        columnView.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CoraRetrievalResult, Button>, ObservableValue<Button>>() {
            @Override
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<CoraRetrievalResult, Button> c) {
                Button b = new Button(txtShow);
                b.setOnAction((ActionEvent e) -> {
                    try {
                        MainApplication.getInstance().getMainAppView().showCase(c.getValue().getCaseId());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
                return new ReadOnlyObjectWrapper<>(b);
            }
        });

        tblResults.setPlaceholder(new Label(txtNoResults));
    }

    private void onAdapt(String selectedCaseId) {
        AdaptionStackController controller = ViewBuilder.getInstance().createDialog(ADAPTION_STACK_VIEW_FILE);

        controller.setOnStartAdaption((startAdaptionEvent) -> {
                List<AdaptionRule> ruleStack = startAdaptionEvent.getRuleStack();
                startAdaptionFor(ruleStack, selectedCaseId);
        });
    }

    public void setRetrievalResults(List<CoraRetrievalResult> results) {
        resultsItems = FXCollections.observableArrayList(results);
        tblResults.setItems(resultsItems);
    }

    private void startAdaptionFor(List<AdaptionRule> ruleStack, String selectedCaseId) {
        AdaptionService adaptionService = new AdaptionService();
        try {
            CoraCaseModel selectedCase = MainApplication.getInstance().getCaseBase().loadCase(selectedCaseId);
            AdaptionService.AdaptionTask task = adaptionService.createAdaptionTask(ruleStack,
                    getQuery(), selectedCase, tblResults.getItems());

            System.out.println("Starting adaption");

            task.stateProperty().addListener((ov, oldState, newState) -> {
                if(newState == Worker.State.SUCCEEDED) {
                    System.out.println("Adaption: Success!");
                    MainApplication.getInstance().getMainAppView().showCase(task.getValue(), "Adaptiert: " + selectedCaseId);
                }

                if(newState == Worker.State.FAILED) {
                    Commons.showException(task.getException());
                }
            });

            new Thread(task).start();
        } catch (Exception e) {
            System.err.println("Konnte ausgewählten Fall nicht laden: " + selectedCaseId);
        }
    }

    @FXML
    private void onChangeFilter() {
        String filterValue = txtFilter.getText();
        if(filterValue.trim().equals("")) {
            tblResults.setItems(resultsItems);
        } else {
            try {
                Float d = Float.parseFloat(filterValue);
                if(d != 0.0) {
                    d /= 100.0f;
                }
                final float val = d;

                FilteredList<CoraRetrievalResult> filteredList;
                filteredList = resultsItems.filtered( model -> (model.getSimilarity() >= val));
                tblResults.setItems(filteredList);
            } catch(NumberFormatException e) {
                tblResults.setItems(resultsItems);
            }
        }
    }

    public CoraQueryModel getQuery() {
        return query;
    }

    public void setQuery(CoraQueryModel query) {
        this.query = query;
    }
}
