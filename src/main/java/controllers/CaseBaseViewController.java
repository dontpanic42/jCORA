package controllers;

import controllers.commons.WaitViewController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import view.Commons;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;


/**
 * Created by daniel on 24.08.14.
 */
public class CaseBaseViewController implements CoraCaseBase.CaseBaseChangeHandler {

    @FXML
    private TableColumn<TableModel, String> tcCaseId;

    @FXML
    private TableColumn<TableModel, Button> tcViewCase;

    @FXML
    private TableColumn<TableModel, Button> tcDeleteCase;

    @FXML
    private TableView<TableModel> tblCaseBase;

    @FXML
    private Button btnImportCase;

    private ObservableList<TableModel> caseList;

    @FXML
    public void initialize() {

        tcViewCase.setCellValueFactory(new PropertyValueFactory<TableModel, Button>("btnView"));
        tcDeleteCase.setCellValueFactory(new PropertyValueFactory<TableModel, Button>("btnDelete"));
        tcCaseId.setCellValueFactory(new PropertyValueFactory<TableModel, String>("caseId"));

        btnImportCase.setOnAction( (ActionEvent e) ->
                CaseImportViewController.showCaseImport(MainApplication.getInstance().getMainStage()));
    }

    public void setCaseBase(CoraCaseBase caseBase) {

        Iterator<String> iter = caseBase.listCaseIDs();
        caseList = FXCollections.observableArrayList();
        while(iter.hasNext()) {
            caseList.add(new TableModel(iter.next()));
        }

        tblCaseBase.setItems(caseList);

        caseBase.addCaseBaseChangeHandler(this);
    }

    @Override
    public void onAddCase(String caseId) {
        TableModel t = new TableModel(caseId);
        if(caseList != null && !caseList.contains(t)) {
            caseList.add(new TableModel(caseId));
        }
    }

    @Override
    public void onRemoveCase(String caseId) {
        TableModel t = new TableModel(caseId);
        if(caseList != null && caseList.contains(t)) {
            caseList.remove(t);
        }
    }

    public class TableModel {
        SimpleStringProperty caseId;
        SimpleObjectProperty<Button> btnView;
        SimpleObjectProperty<Button> btnDelete;

        public TableModel(final String caseId) {
            this.caseId = new SimpleStringProperty();
            this.caseId.set(caseId);

            btnView = new SimpleObjectProperty<Button>(new Button("Anzeigen"));
            btnDelete = new SimpleObjectProperty<Button>(new Button("Löschen"));

            btnView.getValue().setOnAction( (ActionEvent e) -> {
                MainAppViewController mainAppView = MainApplication.getInstance().getMainAppView();

                try {
                    mainAppView.showCase(caseId);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        }

        public String getCaseId() {
            return caseId.get();
        }

        public SimpleStringProperty caseIdProperty() {
            return caseId;
        }

        public void setCaseId(String caseId) {
            this.caseId.set(caseId);
        }

        public Button getBtnView() {
            return btnView.get();
        }

        public SimpleObjectProperty<Button> btnViewProperty() {
            return btnView;
        }

        public void setBtnView(Button btnView) {
            this.btnView.set(btnView);
        }

        public Button getBtnDelete() {
            return btnDelete.get();
        }

        public SimpleObjectProperty<Button> btnDeleteProperty() {
            return btnDelete;
        }

        public void setBtnDelete(Button btnDelete) {
            this.btnDelete.set(btnDelete);
        }

        @Override
        public int hashCode() {
            return caseId.hashCode();
        }
    }
}
