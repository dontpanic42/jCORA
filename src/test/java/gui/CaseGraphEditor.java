package gui;

import com.hp.hpl.jena.ontology.OntModel;
import models.ontology.CoraOntologyModelFactory;
import models.ontology.CoraInstanceModel;
import view.graphview.GraphViewComponent;

import javax.swing.*;

/**
 * Created by daniel on 23.08.14.
 */
public class CaseGraphEditor {

    private CoraOntologyModelFactory factory;
    private OntModel domainModel;

    private CoraInstanceModel[] getCaseRoot() {
//        OntModelSpec spec = new OntModelSpec(PelletReasonerFactory.THE_SPEC);
//
//        domainModel = ModelFactory.createOntologyModel(spec);
//        domainModel.read(FileManager.get().open("src/test/resources/classTestOntology.owl"), "RDF/XML");
//
//        factory = new CoraOntologyModelFactory(domainModel);
//
//        Individual i = domainModel.getIndividual(domainModel.getNsPrefixURI("") + "TestCaseFall");
//        if(i == null) {
//            return null;
//        }
//
//        CoraInstanceModel caseRoot = factory.wrapInstance(i);
//
//        i = domainModel.getIndividual(domainModel.getNsPrefixURI("") + "TestCaseLoesung");
//        if(i == null) {
//            return null;
//        }
//
//        CoraInstanceModel caseSecond = factory.wrapInstance(i);
//        CoraInstanceModel[] ar = {caseRoot, caseSecond};
//
//        return ar;

        return null;
    }

    public void testGraphEditor() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        GraphViewComponent igc = new GraphViewComponent();
        frame.getContentPane().add(igc);
        frame.pack();

        frame.setVisible(true);

        CoraInstanceModel[] instances = getCaseRoot();
        if(instances == null) {
            System.err.println("Instanz nicht gefunden");
            return;
        }

        //igc.addTest(instances[0], instances[1]);
        //igc.addRoot(instances[0]);

        //igc.addBogusGraphNodes();



        igc.createGraphFromInstance(instances[0]);
    }

    public static void main(String[] args) {
        CaseGraphEditor c = new CaseGraphEditor();
        c.testGraphEditor();
    }
}
