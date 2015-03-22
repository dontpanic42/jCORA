package controllers.settings.panes;

import controllers.commons.ThrowableErrorViewController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import view.Commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Created by daniel on 20.03.15.
 */
public class CaseBaseSettingsViewController {

    private static final String CASE_BASE_DEFAULTS = "config/casebase.properties";
    private final Preferences prefs = Preferences.userNodeForPackage(CoraCaseBase.class);

    @FXML
    private TextField txtTDB;

    @FXML
    private TextField txtDomain;

    @FXML
    private TextField txtNS;

    @FXML
    private void initialize() {
        load();
    }

    @FXML
    private void onSelectTDB() {
        DirectoryChooser chooser = new DirectoryChooser();
        File defaultDir = new File(txtTDB.getText());
        if(!defaultDir.exists()) {
            defaultDir = new File(MainApplication.getApplicationPath());
        }

        chooser.setInitialDirectory(defaultDir);

        File newDir = chooser.showDialog(null);
        if(newDir != null) {
            txtTDB.setText(newDir.getAbsolutePath());
        }
    }

    @FXML
    private void onSelectDomain() {
        FileChooser chooser = new FileChooser();

        File init = new File(txtDomain.getText());
        if(!init.exists()) {
            init = new File(MainApplication.getApplicationPath() + "domain.owl");
        }

        if(init.exists() && init.getParentFile().isDirectory()) {
            chooser.setInitialDirectory(init.getParentFile());
        } else {
            chooser.setInitialDirectory(new File(MainApplication.getApplicationPath()));
        }

        chooser.setInitialFileName(init.getName());

        File newFile = chooser.showOpenDialog(null);
        if(newFile != null) {
            txtDomain.setText(newFile.getAbsolutePath());
        }
    }

    public void save() {
        if(new File(txtTDB.getText()).exists()) {
            prefs.put("tdbCaseBase", txtTDB.getText());
        } else {
            System.err.println("Error: Directory " + txtTDB.getText() + " does not exist.");
        }

        if(new File(txtDomain.getText()).exists()) {
            prefs.put("domainModelFile", txtDomain.getText());
        } else {
            System.err.println("Error: File " + txtDomain.getText() + " does not exist.");
        }

        prefs.put("domainNsOverride", txtNS.getText());
    }

    private void load() {
        try {
            Properties caseBaseDefaults = new Properties();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(CASE_BASE_DEFAULTS);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
            caseBaseDefaults.load(reader);
            reader.close();
            is.close();

            txtTDB.setText(prefs.get("tdbCaseBase", MainApplication.getApplicationPath() + caseBaseDefaults.getProperty("tdbCaseBase")));

            txtDomain.setText(prefs.get("domainModelFile", MainApplication.getApplicationPath() + caseBaseDefaults.getProperty("domainModelFile")));

            txtNS.setText(prefs.get("domainNsOverride", caseBaseDefaults.getProperty("domainNsOverride")));

        } catch (Exception e) {
            Commons.showFatalException(e);
        }
    }

    public void reset() {
        try {
            Properties caseBaseDefaults = new Properties();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(CASE_BASE_DEFAULTS);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
            caseBaseDefaults.load(reader);
            reader.close();
            is.close();

            txtTDB.setText(MainApplication.getApplicationPath() + caseBaseDefaults.getProperty("tdbCaseBase"));

            txtDomain.setText(MainApplication.getApplicationPath() + caseBaseDefaults.getProperty("domainModelFile"));

            txtNS.setText(caseBaseDefaults.getProperty("domainNsOverride"));

        } catch (Exception e) {
            Commons.showFatalException(e);
        }
    }

}
