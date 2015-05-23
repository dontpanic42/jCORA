package controllers;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import controllers.commons.ThrowableErrorViewController;
import controllers.commons.TranslateStringViewController;
import exceptions.ResourceAlreadyExistsException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mainapp.MainApplication;
import models.ontology.CoraClassModel;
import models.ontology.CoraInstanceModel;
import view.viewbuilder.ViewBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Dialog, der das Erstellen von Instanzen in einem Fall ermöglicht.
 *
 * Created by daniel on 25.08.14.
 */
public class AddInstanceViewController {

    /**
     * FXML-Datei des AddInstanceView-Dialogs
     */
    private static final String ADD_INSTANCE_VIEW_FILE = "views/addInstanceView.fxml";

    /**
     * Der Baum, in dem die verfügbaren Klassen und Unterklassen angzeigt werden, von denen
     * Instanzen erstellt werden dürfen.
     */
    @FXML
    private TreeView<CoraClassModel> classTree;
    /**
     * Textfeld für die Eingabe des eindeutigen Namen der Instanz
     */
    @FXML
    private TextField txtInstanceName;
    /**
     * Textfeld für die Eingabe des Anzeigenamen. Ist standardmäßig deaktiviert und wird vom
     * Übersetzungs-Editor gesetzt.
     */
    @FXML
    private TextField txtInstanceLabel;

    /**
     * Deaktiviertes Feld, das den ausgewählten Klassennamen der Instanz anzeigt
     */
    @FXML
    private TextField txtClassName;
    /**
     * Die Instanz, die angelegt wurde, oder null, falls (noch) keine Instanz angelegt wurde
     */
    private CoraInstanceModel returnValue = null;
    /**
     * Die Stage in der dieser Dialog angzeigt wird
     */
    private Stage stage;

    /* NEU */
    private OntModel model;

    @FXML
    private CheckBox clsActive;

    private Set<CoraClassModel> superClasses;
    /* NEU ENDE */

    /**
     * Liste der Überseztungen des Anzeigenamen
     */
    private Map<String, String> translations;

    /**
     * Zeigt diesen Dialog an. Blockiert so lange, bis die Erstellung der Instanz abgeschlossen- oder der
     * Vorgang abgebrochen wurde. Gibt eine neue Instanz oder <code>null</code> zurück.
     * @param parent Die Stage, zu der dieser Dialog modal ist
     * @param superClasses Set der Klassen, die als "Root" für den Klassenbaum dienen
     * @return Eine neu angelegte Instanz, oder <code>null</code>, falls keine Instanz angelegt wurde
     * @throws IOException Falls die FXML-Datei nicht gefunden wurde
     */
    public static CoraInstanceModel showCreateInstance(Stage parent, Set<CoraClassModel> superClasses) throws IOException {
        FXMLLoader loader = ViewBuilder.getInstance().createLoader(ADD_INSTANCE_VIEW_FILE);
        AnchorPane pane = loader.load();

        Stage stage = new Stage();
        stage.setTitle(ViewBuilder.getInstance().getText("ui.add_instance.title"));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parent);

        Scene scene = new Scene(pane);
        stage.setScene(scene);

        AddInstanceViewController c = loader.getController();
        c.setSuperClasses(superClasses);
        c.setStage(stage);

        /* NEU
        * TODO: Model direkt übergeben... */
        for(CoraClassModel cls : superClasses) {
            c.setModel(cls);
            break;
        }
        /* NEU ENDE */

        stage.showAndWait();

        return c.getReturnValue();
    }

    /**
     * Getter für die neu anglegte Instanz, wird von <code>AddInstanceViewController#showCreateInstance</code>
     * verwendet.
     * @return Die neu angelegte Instanz oder null
     */
    private CoraInstanceModel getReturnValue() {
        return returnValue;
    }

    /**
     * Setter für die neu angelegte Instanz
     * @param instance Die Instanz, die neu angelegt wurde
     */
    private void setReturnValue(CoraInstanceModel instance) {
        returnValue = instance;
    }

    /**
     * Setzt die Stage, in der dieser Dialog angzeigt wird
     * @param stage die Stage, in der dieser Dialog angzeigt wird
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Setzt Klassen, von denen (oder von deren Unterklassen) Instanzen gebildet
     * werden dürfen.
     * @param superClasses Set der Klassen, die als "Root" für den Klassenbaum dienen
     */
    public void setSuperClasses(Set<CoraClassModel> superClasses) {
        this.superClasses = superClasses;
        TreeItem<CoraClassModel> dummyRoot = new TreeItem<>();

        for(CoraClassModel model : superClasses) {
            ClassTreeItem item = new ClassTreeItem(model);
            dummyRoot.getChildren().add(item);
        }

        classTree.setRoot(dummyRoot);
    }

    /* Neu
    * TODO: Ordnetlich machen!!*/
    public void setModel(CoraClassModel superClass) {
        this.model = superClass.getModel();
    }

    @FXML
    private void onChangeClsActive() {
        System.out.println("clsACTIVE: ");
        if(clsActive.isSelected()) {
            System.out.println("Ja.");
            txtClassName.setEditable(true);
        } else {
            System.out.println("Nein.");
            txtClassName.setEditable(false);
        }
    }
    /* Neu ende */

    @FXML
    public void initialize() {
        // Zeige keinen Knoten an, da mehrere Klassen auf der selben Ebene verfügbar sein könnten
        classTree.setShowRoot(false);
        // Eventhandler für die Auswahl einer Klasse
        classTree.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            CoraClassModel mOld = (oldValue != null)?
                    newValue.valueProperty().get() : null;
            CoraClassModel mNew = (newValue != null)?
                    newValue.valueProperty().get() : null;

            onTreeSelectionChange(mNew);
        });

        // CellFactory, die Knoten vom Typ <code>AddInstanceViewController.ClassTreeItem</code> anzeigt
        classTree.setCellFactory(new Callback<TreeView<CoraClassModel>, TreeCell<CoraClassModel>>() {
            @Override
            public TreeCell<CoraClassModel> call(TreeView<CoraClassModel> coraClassModelTreeView) {
                return new TreeCell<CoraClassModel>() {
                    @Override
                    protected void updateItem(CoraClassModel coraClassModel, boolean empty) {
                        super.updateItem(coraClassModel, empty);

                        if (empty) {
                            setText("");
                        } else {
                            final String lang = MainApplication.getInstance().getLanguage();
                            setText(coraClassModel.getDisplayName(lang));
                        }
                    }
                };
            }
        });

    }

    /**
     * Zeigt den Namen der im Baum ausgewählten Klasse im Textfeld
     * <code>txtClassName</code> an.
     * @param newValue Die ausgewählte Klasse
     */
    private void onTreeSelectionChange(CoraClassModel newValue) {
        if(newValue != null) {
            final String lang = MainApplication.getInstance().getLanguage();
            txtClassName.setText(newValue.getDisplayName(lang));
        } else {
            txtClassName.setText("(Klasse wählen)");
        }
    }

    /**
     * Schließt den Dialog ohne das die Instanz gespeichert wird.
     */
    @SuppressWarnings("unused")
    @FXML
    private void onCancel() {
        this.stage.close();
    }

    /**
     * Filtert den angegebenen eindeutigen Namen der Instanz, da Leerzeichen und andere
     * Sonderzeichen nicht erlaubt sind. Sonderzeichen und Leerzeichen werden durch
     * Unterstriche ("_") ersetzt.
     * Diese Methode ist möglicherweise aggressiver als notwendig, da grundsätzlich die
     * meisten UTF-8-Zeichen erlaubt sein sollte (bis auf leerzeichen). Aber sicher ist besser :-)
     * @param name Der Name, der gefiltert werden soll
     * @return Der gefilterte name
     */
    private String escapeInstanceName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * Erzeugt eine neue Instanz aus den Eingegebenen Daten
     */
    @SuppressWarnings("unused")
    @FXML
    private void onCreateInstance() {
        /* Hole den Instanznamen */
        String instanceName = escapeInstanceName(txtInstanceName.getText());

        /* Ist der Instanzname leer, breche ab */
        if(instanceName.equals("")) {
            System.err.println("Kein Instanzname angegeben");
            return;
        }

        /* Bestimme die Elternklasse */
        CoraClassModel clazz = null;
        /* Ist Cora Logic Script (CLS) aktiviert, versuche den Klassennamen zu parsen */
        if(clsActive.isSelected()) {
            try {
                OntClass oc = services.cls.ClassBuilder.createClassFromString(model, txtClassName.getText());
                CoraClassModel ccm = superClasses.iterator().next();
                clazz = ccm.getFactory().wrapClass(oc);

            } catch(Exception e) {
                ThrowableErrorViewController.showError(e, true);
            }
        /* Ist CLS nicht aktiv, verwende die im Baum ausgewählte Klasse. */
        } else {
            clazz = classTree.getSelectionModel()
                    .getSelectedItem().valueProperty().get();
        }

        /* Ist die Klasse gleich null, d.h. es wurde keine Klasse im Baum ausgewählt oder der
           der CLS-String ist fehlerhaft, breche ab. */
        if(clazz == null) {
            System.err.println("Keine klasse ausgewählt.");
            return;
        }

        /* Erzeuge die Instanz */
        try {
            CoraInstanceModel instance;

            if(getTranslations() == null) {
                //Wenn kein Label angegeben wurde, erzeuge Instanz ohne Label
                instance = clazz.getFactory().createInstance(clazz, instanceName);
            } else {
                //Wenn ein Label angegeben wurde, nehme an, das dies in der aktuellen Sprache ist
                instance = clazz.getFactory().createInstance(clazz, instanceName, getTranslations());
            }
            setReturnValue(instance);
            stage.close();
        } catch (ResourceAlreadyExistsException e) {
            //TODO: Wenn die Klasse mit CLS erzeugt wurde, lösche diese wieder!
            System.err.println("Instanz existiert bereits");
        }
    }

    /**
     * Ruft den Dialog für das Editieren des Anzeigeinamens auf.
     */
    @SuppressWarnings("unused")
    @FXML
    private void onTranslateString() {
        try {
            setTranslations(TranslateStringViewController.getStringTranslation(txtInstanceLabel.textProperty(), getTranslations()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter für alle anglegten Übersetzungen des Anzeigenamen
     * @return Liste mit Übersetzungen
     */
    public Map<String, String> getTranslations() {
        return translations;
    }

    /**
     * Setter für die Übersetzungen des Anzeigenamen
     * @param translations Die Überseztungen
     */
    public void setTranslations(Map<String, String> translations) {
        this.translations = translations;
    }

    /**
     * Datenmodell für die Knoten im Klassen-Baum
     */
    public class ClassTreeItem extends TreeItem<CoraClassModel> {

        /**
         * Ist <code>false</code>, wenn die Kindknoten bereits ermittelt wurden.
         * Andernfalls müssen die kindknoten erst mit <code>ClassTreeItem#buildChildren()</code>
         * erzeugt werden.
         */
        private boolean isFirstRequest = true;

        /**
         * Konstruktor des Knotens
         * @param model Klasse, die dieser Knoten repräsentiert
         */
        public ClassTreeItem(CoraClassModel model) {
            valueProperty().setValue(model);
        }

        /**
         * Gibt die Kinder (= die Subklassen) zurück
         * @return Kinder dieses Knotens
         */
        @Override
        public ObservableList<TreeItem<CoraClassModel>> getChildren() {
            if(isFirstRequest) {
                isFirstRequest = false;
                super.getChildren().setAll(buildChildren());
            }

            return super.getChildren();
        }

        /**
         * Erstellt eine Liste der Kindknoten. Diese wird erst erstellt, wenn die Kind-Knoten
         * zum ersten Mal benötigt werden (somit ist ein vollständiges Traviersieren der Klassen-
         * Hierarchie beim Erstellen des Baums nicht notwendig)
         * @return
         */
        public ObservableList<TreeItem<CoraClassModel>> buildChildren() {
            ObservableList<TreeItem<CoraClassModel>> children;
            Set<CoraClassModel> set = valueProperty().get().getFlattenedChildren();
            children = FXCollections.observableArrayList();

            for(CoraClassModel c : set) {
                children.add(new ClassTreeItem(c));
            }

            return children;
        }

        /**
         * Gibt zurück, ob diese Knoten ein Blatt ist.
         * @return
         */
        @Override
        public boolean isLeaf() {
            if(isFirstRequest) {
                isFirstRequest = false;
                super.getChildren().setAll(buildChildren());
            }

            return (super.getChildren().size() == 0);
        }
    }
}
