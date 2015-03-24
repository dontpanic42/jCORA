package controllers;

//import apple.laf.JRSUIUtils;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import view.Commons;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;
import java.util.List;
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

    /**
     * Textfeld für die Suche nach Fall-IDs
     */
    @FXML
    private TextField txtSearchCaseID;
    /**
     * Items der Liste der Fälle
     */
    private ObservableList<TableModel> itemsCases;
    /**
     * Asynchroner <code>Task</code> für das laden der Fallbasis
     */
    private Task<ObservableList<TableModel>> loadCaseBaseTask;

    @FXML
    private Accordion accordionLeft;

    /**
     * Property, das die aktuell angzeigte Fallbasis (oder null, wenn noch keine Fallbasis geladen wurde) enthält.
     */
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

    /**
     * Zeigt den Case-Import Dialog an
     */
    @SuppressWarnings("unused")
    @FXML
    private void onCaseImport() {
        CaseImportViewController.showCaseImport(MainApplication.getInstance().getMainStage());
    }

    /**
     * Zeigt eine Fallbasis in dieser Ansicht an (asynchron)
     * @param caseBase Die anzuzeigende Fallbasis
     */
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

    /**
     * Sucht nach einer Fall-ID, die (teilweise) durch das Textfeld
     * <code>txtSearchCaseID</code> vorgegebn wird.
     */
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

    /**
     * Wird aufgerufen, wenn ein neuer Fall erstellt wird (durch Speichern eines Falls mit einer neuen ID).
     * Fügt den neu erstellten Fall in der Liste der Fälle an
     * @throws Exception
     */
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

    /**
     * Entfernt eine Fall mit der ID <code>caseId</code> aus der Liste
     * der angezeigten Fälle
     * @param caseId
     */
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

    /**
     * Gibt die derzeit angezeigte Fallbasis zurück.
     * @return Die derzeit angezeigte Fallbasis
     */
    public CoraCaseBase getCaseBase() {
        return caseBase.get();
    }

    /**
     * Property, das die derzeit angezeigte Fallbasis enthält.
     * @return Property, das die derzeit angezeigte Fallbasis enthält.
     */
    public SimpleObjectProperty<CoraCaseBase> caseBaseProperty() {
        return caseBase;
    }

    /**
     * Setzt die anzuzeigende Fallbasis
     * @param caseBase
     */
    public void setCaseBase(CoraCaseBase caseBase) {
        this.caseBase.set(caseBase);
    }

    /**
     * Datenmodell für die Fall-Liste.
     */
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

            return caseId.getValue().equals(that.caseId.getValue());

        }
    }
}
