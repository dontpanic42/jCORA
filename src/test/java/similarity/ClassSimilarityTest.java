package similarity;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import models.ontology.CoraOntologyModelFactory;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseBaseImpl;
import models.cbr.CoraCaseModel;
import models.cbr.CoraCaseModelImpl;
import models.ontology.CoraClassModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.retrieval.similarity.functions.ontological.SimilarityClass;

import javax.naming.ConfigurationException;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by daniel on 31.08.14.
 */
public class ClassSimilarityTest {

    CoraCaseBase caseBase;
    CoraCaseModel caseA;
    CoraCaseModel caseB;

    @Before
    public void testSetup() {
        CoraCaseBase caseBase = null;

        try {
            caseBase = new CoraCaseBaseImpl();
            this.caseBase = caseBase;
        } catch (ConfigurationException | FileNotFoundException e) {
            fail(e.getMessage());
        }

        try {
            caseA = caseBase.createTemporaryCase();
            caseB = caseBase.createTemporaryCase();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            fail(throwable.getMessage());
        }
    }

    @Test
    public void testPathsToRoot() {
        assertNotNull(caseA);

        CoraCaseModelImpl caseImpl = (CoraCaseModelImpl) caseA;
        CoraOntologyModelFactory factory = caseImpl.getCaseDescription().getFactory();

        OntModel model = caseImpl.getModel();
        String NS = "http://www.semanticweb.org/daniel/ontologies/2014/7/untitled-ontology-270#";

        OntClass cls1 = model.getOntClass(NS + "ClassSimilarity2");
        OntClass cls2 = model.getOntClass(NS + "AndTestClass");

        assertNotNull(cls1);
        assertNotNull(cls2);

        CoraClassModel clsM1 = factory.wrapClass(cls1);
        CoraClassModel clsM2 = factory.wrapClass(cls2);

        SimilarityClass simFunc = new SimilarityClass();
        List<SimilarityClass.Path> pathsToRoot;

        pathsToRoot = simFunc.getPathsToRoot(clsM1);
        //Es sollte genau ein Pfad existieren.
        assertTrue(pathsToRoot.size() == 1);
        //Der Pfad sollte 4 elemente lang sein.
        //Pfad: ClassSimilarity2 -> ClassSimilarity1 -> ClassSimilarityRoot (-> OWL:Thing)
        assertTrue(pathsToRoot.get(0).getPathLength() == 4);

        pathsToRoot = simFunc.getPathsToRoot(clsM2);
        //AndTestClass hat zwei pfade zu Root, über AndClass1 und AndClass2:
        assertTrue(pathsToRoot.size() == 2);
        //Die Pfade sollten jeweils 4 elemente lang sein
        assertTrue(pathsToRoot.get(0).getPathLength() == 4);
        assertTrue(pathsToRoot.get(1).getPathLength() == 4);

        OntClass cls3 = model.getOntClass(NS + "OrClass");
        assertTrue(cls3 != null);
        CoraClassModel clsM3 = factory.wrapClass(cls3);
        pathsToRoot = simFunc.getPathsToRoot(clsM3);
        //OrClass hat nur AndSuperClass als Elternelement
        assertTrue(pathsToRoot.size() == 1);
        assertTrue(pathsToRoot.get(0).getPathLength() == 3);
    }

    @Test
    public void testLCSDepth() {
        assertNotNull(caseA);

        CoraCaseModelImpl caseImpl = (CoraCaseModelImpl) caseA;
        CoraOntologyModelFactory factory = caseImpl.getCaseDescription().getFactory();

        OntModel model = caseImpl.getModel();
        String NS = "http://www.semanticweb.org/daniel/ontologies/2014/7/untitled-ontology-270#";

        OntClass cls1 = model.getOntClass(NS + "OrClass");
        OntClass cls2 = model.getOntClass(NS + "AndTestClass");

        assertNotNull(cls1);
        assertNotNull(cls2);

        CoraClassModel clsM1 = factory.wrapClass(cls1);
        CoraClassModel clsM2 = factory.wrapClass(cls2);

        SimilarityClass simFunc = new SimilarityClass();

        //Die LCS-Tiefe der selben Klasse sollte = 1 sein.
        assertTrue(simFunc.getLCSDepth(clsM1, clsM1) == 1);

        // LCSDepth(OrClass, AndTestClass) sollte 3 sein:
        // Pfad: AndTestClass -> AndClass1 -> (LCS: ) AndSuperClass (-> OWL:Thing) : 3
        // Pfad: AndTestClass -> AndClass2 -> (LCS: ) AndSuperClass (-> OWL:Thing) : 3
        // Pfad: OrClass -> AndSuperClass (-> OWL:Thing) : 2
        assertTrue(simFunc.getLCSDepth(clsM1, clsM2) == 3);

        OntClass cls3 = model.getOntClass(NS + "ComplexClass");
        assertNotNull(cls3);
        CoraClassModel clsM3 = factory.wrapClass(cls3);
        // LCSDepth(OrClass, ComplexClass) sollte = 3 sein
        // Pfad: OrClass -> AndSuperClass (-> (LCS: ) OWL:Thing) : 3
        // Pfad: ComplexClass (-> (LCS: ) OWL:Thing) : 2
        assertTrue(simFunc.getLCSDepth(clsM1, clsM3) == 3);
    }

    @After
    public void teardownTest() {
        if(caseA != null) {
            caseA.close();
        }

        if(caseB != null) {
            caseB.close();
        }

        if(caseBase != null) {
            caseBase.close();
        }
    }
}
