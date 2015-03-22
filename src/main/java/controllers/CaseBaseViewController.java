package controllers;

//import apple.laf.JRSUIUtils;
import controllers.commons.ThrowableErrorViewController;
import controllers.commons.WaitViewController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import view.Commons;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.ResourceBundle;


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
    private TextField txtSearchCaseID;
    private ObservableList<TableModel> itemsCases;
    private Task<ObservableList<TableModel>> loadCaseBaseTask;

    @FXML
    private Accordion accordionLeft;

    private SimpleObjectProperty<CoraCaseBase> caseBase = new SimpleObjectProperty<>();

    @FXML
    public void initialize() {

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        spinner.setMaxSize(50.0, 50.0);
        tblCaseBase.setPlaceholder(spinner);

        tcViewCase.setCellValueFactory(new PropertyValueFactory<>("btnView"));
        tcDeleteCase.setCellValueFactory(new PropertyValueFactory<>("btnDelete"));
        tcCaseId.setCellValueFactory(new PropertyValueFactory<>("caseId"));

        if(accordionLeft.getPanes().size() > 0) {
            accordionLeft.setExpandedPane(accordionLeft.getPanes().get(0));
        }

        caseBase.addListener((ov, oldCaseBase, newCaseBase) -> {
            if(newCaseBase != null) {
                showCaseBase(newCaseBase);
            }
        });
    }

    @SuppressWarnings("unused")
    @FXML
    private void onCaseImport() {
        CaseImportViewController.showCaseImport(MainApplication.getInstance().getMainStage());
    }

    private void showCaseBase(CoraCaseBase caseBase) {
        if(loadCaseBaseTask != null) {
            loadCaseBaseTask.cancel();
        }

        loadCaseBaseTask = new Task<ObservableList<TableModel>>() {
            @Override
            protected ObservableList<TableModel> call() throws Exception {
                List<String> ids = caseBase.getCaseIDs();
                ObservableList<TableModel> tblModels = FXCollections.observableArrayList();

                for(String id : ids) {
                    tblModels.add(new TableModel(id));
                }

                return tblModels;
            }
        };

        loadCaseBaseTask.stateProperty().addListener((ov, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                itemsCases = loadCaseBaseTask.getValue();
                tblCaseBase.setItems(itemsCases);

                String placeholderText = ViewBuilder.getInstance().getText("ui.case_base_placeholder_empty");
                tblCaseBase.setPlaceholder(new Label(placeholderText));
            } else if(newState == Worker.State.FAILED) {
                String placeholderText = ViewBuilder.getInstance().getText("ui.case_base_placeholder_error_loading");
                tblCaseBase.setPlaceholder(new Label(placeholderText));
                Commons.showFatalException(loadCaseBaseTask.getException());
            }
        });

        new Thread(loadCaseBaseTask).start();
        caseBase.addCaseBaseChangeHandler(this);
    }

    @SuppressWarnings("unused")
    @FXML
    private void onSearchCaseID() {
        String toSearch = txtSearchCaseID.getText().toLowerCase();
        if(toSearch.equals("")) {
            tblCaseBase.setItems(itemsCases);
        } else {
            FilteredList<TableModel> filtered;
            filtered = itemsCases.filtered( model -> model.getCaseId().toLowerCase().contains(toSearch));
            tblCaseBase.setItems(filtered);
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void onCreateNewCase() throws Exception {
        CoraCaseModel caseModel = MainApplication.getInstance()
                .getCaseBase().createTemporaryCase();
        SaveAsNewViewController.showSaveAsNew(MainApplication.getInstance()
                .getMainStage(), caseModel);
    }

    @Override
    public void onAddCase(String caseId) {
        TableModel t = new TableModel(caseId);
        if(itemsCases != null && !itemsCases.contains(t)) {
            itemsCases.add(new TableModel(caseId));
        }
    }

    @Override
    public void onRemoveCase(String caseId) {
        System.out.println("On Case Remove..." + caseId);
        TableModel t = new TableModel(caseId);

        System.out.println("Does contain?" + itemsCases.contains(t));

        if(itemsCases != null && itemsCases.contains(t)) {
            System.out.println("Removing");
            itemsCases.remove(t);
        }
    }

    public CoraCaseBase getCaseBase() {
        return caseBase.get();
    }

    public SimpleObjectProperty<CoraCaseBase> caseBaseProperty() {
        return caseBase;
    }

    public void setCaseBase(CoraCaseBase caseBase) {
        this.caseBase.set(caseBase);
    }

    public class TableModel {
        SimpleStringProperty caseId;
        SimpleObjectProperty<Button> btnView;
        SimpleObjectProperty<Button> btnDelete;

        public TableModel(final String caseId) {
            this.caseId = new SimpleStringProperty();
            this.caseId.set(caseId);

            ResourceBundle texts = MainApplication.getInstance().getTexts();

            btnView = new SimpleObjectProperty<>(new Button(texts.getString("ui.case_base_view.btn_show_case")));
            btnDelete = new SimpleObjectProperty<>(new Button(texts.getString("ui.case_base_view.btn_delete_case")));

            btnView.getValue().setOnAction( (ActionEvent e) -> {
                MainAppViewController mainAppView = MainApplication.getInstance().getMainAppView();

                try {
                    mainAppView.showCase(caseId);
                } catch (IOException e1) {
                    Commons.showException(e1);
                }
            });

            btnDelete.getValue().setOnAction((ActionEvent e) -> {
                CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();
                caseBase.removeCase(caseId);
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
            return caseId.getValue().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TableModel)) return false;

            TableModel that = (TableModel) o;

            if (!caseId.getValue().equals(that.caseId.getValue())) return false;

            return true;
        }
    }
}
