package controllers.retrieval;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.cbr.CoraRetrievalResult;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by daniel on 02.09.14.
 */
public class RetrievalResultsViewController {

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

    private ObservableList<CoraRetrievalResult> resultsItems;

    @FXML
    private void initialize() {
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
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<CoraRetrievalResult, Button> coraRetrievalResultButtonCellDataFeatures) {
                Button b = new Button("Adaptieren");
                b.setOnAction( (ActionEvent e ) -> System.out.println("Adaptieren..."));
                return new ReadOnlyObjectWrapper<>(b);
            }
        });

        columnView.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CoraRetrievalResult, Button>, ObservableValue<Button>>() {
            @Override
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<CoraRetrievalResult, Button> c) {
                Button b = new Button("Anzeigen");
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

        tblResults.setPlaceholder(new Label("Keine anzeigbaren Ergebnisse."));
    }

    public void setRetrievalResults(List<CoraRetrievalResult> results) {
        resultsItems = FXCollections.observableArrayList(results);
        tblResults.setItems(resultsItems);
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
}
