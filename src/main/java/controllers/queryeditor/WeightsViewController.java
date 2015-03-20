package controllers.queryeditor;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.cbr.CoraCaseModel;
import models.cbr.CoraWeightModel;
import models.ontology.CoraPropertyModel;
import view.Commons;
import view.viewbuilder.ViewBuilder;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Created by daniel on 08.12.14.
 */
public class WeightsViewController {

    @FXML
    private TableView<WeightColumn> tableView;

    @FXML
    private TableColumn<WeightColumn, String> propertyColumn;

    @FXML
    private TableColumn<WeightColumn, Slider> weightColumn;

    @FXML
    private TableColumn<WeightColumn, Double> valueColumn;

    private Task<ObservableList<WeightColumn>> listPropertiesTask;

    private ObjectProperty<CoraCaseModel> caseModel = new SimpleObjectProperty<>();

    private final FileChooser fileChooser = new FileChooser();

    @SuppressWarnings("unused")
    @FXML
    private void initialize() {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        spinner.setMaxSize(50.0, 50.0);
        tableView.setPlaceholder(spinner);

        propertyColumn.setCellValueFactory(new PropertyValueFactory<>("attributName"));
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("slider"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        caseModel.addListener((ov, oldValue, newValue) -> {
            if(newValue != null) {
                onChangeCaseModel(newValue);
            }
        });
    }

    /**
     * Wird aufgerufen, wenn das aktuelle Gewichtsprofil gesichert werden soll.
     */
    @FXML
    private void onSaveProfile() {
        fileChooser.setInitialFileName("profile.cwp");
        fileChooser.setTitle(ViewBuilder.getInstance().getText("ui.weights.dialog_save_profile"));
        File file = fileChooser.showSaveDialog(null);
        if(file == null) {
            return;
        }

        Properties p = new Properties();
        String key;
        Double val;
        for(WeightColumn w : tableView.getItems()) {
            key = w.getAttribut().toString();
            val = w.getValue();
            p.put(key, val.toString());
        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            p.store(out, "CORA Weight Profile. Do not edit manually.");
        } catch (FileNotFoundException e) {
            Commons.showException(e);
        } catch (IOException e) {
            Commons.showException(e);
        } finally {
            if(out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Wird aufgerufen, wenn ein gesichertes Gewichtsprofil geladen werden soll.
     */
    @FXML
    private void onLoadProfile() {
        fileChooser.setInitialFileName("profile.cwp");
        fileChooser.setTitle(ViewBuilder.getInstance().getText("ui.weights.dialog_load_profile"));

        File file = fileChooser.showOpenDialog(null);
        if(file == null) {
            return;
        }

        Properties p = new Properties();
        InputStream is = null;

        try {
            is = new FileInputStream(file);
            p.load(is);

            String key;
            Double val;
            for(WeightColumn w : tableView.getItems()) {
                key = w.getAttribut().toString();
                if(p.containsKey(key)) {
                    try {
                        val = Double.valueOf((String) p.get(key));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        continue;
                    }

                    w.getSlider().setValue(val);
                }
            }
        } catch (FileNotFoundException e) {
            Commons.showException(e);
        } catch (IOException e) {
            Commons.showException(e);
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Aufgerufen, wenn der '>>' Button gedrückt wird.
     * Setzt alle Gewichte auf 100.
     */
    @FXML
    private void onSetFullWeights() {
        for(WeightColumn w : tableView.getItems()) {
            w.getSlider().setValue(100.);
        }
    }

    /**
     * Aufgerufen, wenn der '<<' Button gedrückt wird.
     * Setzt alle Gewichte auf 0.
     */
    @FXML
    private void onSetZeroWeights() {
        for(WeightColumn w : tableView.getItems()) {
            w.getSlider().setValue(0.);
        }
    }

    private void onChangeCaseModel(final CoraCaseModel newCaseModel) {
        if(listPropertiesTask != null) {
            listPropertiesTask.cancel();
        }

        listPropertiesTask = new Task<ObservableList<WeightColumn>>() {
            @Override
            protected ObservableList<WeightColumn> call() throws Exception {
                List<CoraPropertyModel> props = newCaseModel.listAllProperties();

                WeightColumn w;
                ObservableList<WeightColumn> results = FXCollections.observableArrayList();
                for(CoraPropertyModel prop : props) {
                    w = new WeightColumn(prop);
                    results.add(w);
                    System.out.println("Adding " + w.getAttributName());
                }

                return results;
            }
        };

        listPropertiesTask.stateProperty().addListener((ov, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                tableView.getItems().setAll(listPropertiesTask.getValue());
                tableView.setPlaceholder(new Label(""));
            } else if(newState == Worker.State.FAILED) {
                Commons.showException(listPropertiesTask.getException());
                tableView.setPlaceholder(new Label(listPropertiesTask.getException().getMessage()));
            }
        });

        new Thread(listPropertiesTask).start();
    }

    public CoraCaseModel getCaseModel() {
        return caseModel.get();
    }

    public ObjectProperty<CoraCaseModel> caseModelProperty() {
        return caseModel;
    }

    public void setCaseModel(CoraCaseModel caseModel) {
        this.caseModel.set(caseModel);
    }

    public CoraWeightModel getWeightModel() {
        CoraWeightModel wm = new CoraWeightModel();
        float val;
        for(WeightColumn w : tableView.getItems()) {
            val = (w.getValue() == 0.) ? 0.f : (float) (w.getValue() / 100.);
            wm.setWeight(w.getAttribut(), val);
        }

        return wm;
    }

    public class WeightColumn {
        private ObjectProperty<CoraPropertyModel> attribut;
        private ObjectProperty<Slider> slider;
        private StringProperty attributName;
        private DoubleProperty value = new SimpleDoubleProperty();

        public WeightColumn(CoraPropertyModel property) {
            attribut = new SimpleObjectProperty<>(property);

            final String lang = MainApplication.getInstance().getLanguage();
            attributName = new SimpleStringProperty(property.getDisplayName(lang));

            Slider slider = new Slider();
            slider.setMin(0.);
            slider.setMax(100.);
            slider.setValue(100.);
            slider.setMajorTickUnit(1.);
            slider.setMinorTickCount(0);
            slider.setSnapToTicks(true);

            value.bind(slider.valueProperty());

            this.slider = new SimpleObjectProperty<>(slider);
        }

        public String getAttributName() {
            return attributName.get();
        }

        public StringProperty attributNameProperty() {
            return attributName;
        }

        public void setAttributName(String attributName) {
            this.attributName.set(attributName);
        }

        public Slider getSlider() {
            return slider.get();
        }

        public ObjectProperty<Slider> sliderProperty() {
            return slider;
        }

        public void setSlider(Slider slider) {
            this.slider.set(slider);
        }

        public double getValue() {
            return value.get();
        }

        public DoubleProperty valueProperty() {
            return value;
        }

        public void setValue(double value) {
            this.value.set(value);
        }

        public CoraPropertyModel getAttribut() {
            return attribut.get();
        }

        public ObjectProperty<CoraPropertyModel> attributProperty() {
            return attribut;
        }

        public void setAttribut(CoraPropertyModel attribut) {
            this.attribut.set(attribut);
        }
    }
}
