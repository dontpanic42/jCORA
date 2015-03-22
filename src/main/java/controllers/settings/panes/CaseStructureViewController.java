package controllers.settings.panes;

import controllers.commons.ThrowableErrorViewController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import models.cbr.CoraCaseModel;
import view.Commons;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Created by daniel on 20.03.15.
 */
public class CaseStructureViewController {

    private static final String CASE_STRUCTURE_DEFAULTS_FILE = "config/casestructure.properties";

    @FXML
    private TextField txtRootClass;
    @FXML
    private TextField txtDescriptionClass;
    @FXML
    private TextField txtSolutionClass;
    @FXML
    private TextField txtJustificationClass;

    @FXML
    private TextField txtHasDescription;
    @FXML
    private TextField txtHasSolution;
    @FXML
    private TextField txtHasJustification;

    @FXML
    private TextField txtRootIndv;
    @FXML
    private TextField txtDescriptionIndv;
    @FXML
    private TextField txtSolutionIndv;
    @FXML
    private TextField txtJustificationIndv;

    private final Preferences prefs = Preferences.userNodeForPackage(CoraCaseModel.class);

    @FXML
    private void initialize() {
        load();
    }

    public void load() {
        try {
            Properties caseStructureMappingDefaults = new Properties();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(CASE_STRUCTURE_DEFAULTS_FILE);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
            caseStructureMappingDefaults.load(reader);
            reader.close();
            is.close();

            txtRootClass            .setText(prefs.get("rootClass", caseStructureMappingDefaults.getProperty("rootClass")));
            txtDescriptionClass     .setText(prefs.get("descriptionClass", caseStructureMappingDefaults.getProperty("descriptionClass")));
            txtSolutionClass        .setText(prefs.get("solutionClass", caseStructureMappingDefaults.getProperty("solutionClass")));
            txtJustificationClass   .setText(prefs.get("justificationClass", caseStructureMappingDefaults.getProperty("justificationClass")));

            txtHasDescription     .setText(prefs.get("hasDescription", caseStructureMappingDefaults.getProperty("hasDescription")));
            txtHasSolution        .setText(prefs.get("hasSolution", caseStructureMappingDefaults.getProperty("hasSolution")));
            txtHasJustification   .setText(prefs.get("hasJustification", caseStructureMappingDefaults.getProperty("hasJustification")));

            txtRootIndv           .setText(prefs.get("rootIndv", caseStructureMappingDefaults.getProperty("rootIndv")));
            txtDescriptionIndv    .setText(prefs.get("descriptionIndv", caseStructureMappingDefaults.getProperty("descriptionIndv")));
            txtSolutionIndv       .setText(prefs.get("solutionIndv", caseStructureMappingDefaults.getProperty("solutionIndv")));
            txtJustificationIndv  .setText(prefs.get("justificationIndv", caseStructureMappingDefaults.getProperty("justificationIndv")));
        } catch (Exception e) {
            Commons.showFatalException(e);
        }
    }

    public void save() {
        prefs.put("rootClass", txtRootClass.getText());
        prefs.put("descriptionClass", txtDescriptionClass.getText());
        prefs.put("solutionClass", txtSolutionClass.getText());
        prefs.put("justificationClass", txtJustificationClass.getText());

        prefs.put("hasDescription", txtHasDescription.getText());
        prefs.put("hasSolution", txtHasSolution.getText());
        prefs.put("hasJustification", txtHasJustification.getText());

        prefs.put("rootIndv", txtRootIndv.getText());
        prefs.put("descriptionIndv", txtDescriptionIndv.getText());
        prefs.put("solutionIndv", txtSolutionIndv.getText());
        prefs.put("justificationIndv", txtJustificationIndv.getText());
    }

    public void reset() {
        try {
            Properties caseStructureMappingDefaults = new Properties();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(CASE_STRUCTURE_DEFAULTS_FILE);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
            caseStructureMappingDefaults.load(reader);
            reader.close();
            is.close();

            txtRootClass            .setText(caseStructureMappingDefaults.getProperty("rootClass"));
            txtDescriptionClass     .setText(caseStructureMappingDefaults.getProperty("descriptionClass"));
            txtSolutionClass        .setText(caseStructureMappingDefaults.getProperty("solutionClass"));
            txtJustificationClass   .setText(caseStructureMappingDefaults.getProperty("justificationClass"));


            txtHasDescription     .setText(caseStructureMappingDefaults.getProperty("hasDescription"));
            txtHasSolution        .setText(caseStructureMappingDefaults.getProperty("hasSolution"));
            txtHasJustification   .setText(caseStructureMappingDefaults.getProperty("hasJustification"));


            txtRootIndv           .setText(caseStructureMappingDefaults.getProperty("rootIndv"));
            txtDescriptionIndv    .setText(caseStructureMappingDefaults.getProperty("descriptionIndv"));
            txtSolutionIndv       .setText(caseStructureMappingDefaults.getProperty("solutionIndv"));
            txtJustificationIndv  .setText(caseStructureMappingDefaults.getProperty("justificationIndv"));

        } catch (Exception e) {
            Commons.showFatalException(e);
        }
    }
}
