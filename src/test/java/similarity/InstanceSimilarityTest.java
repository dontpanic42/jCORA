package similarity;

import models.cbr.*;
import models.ontology.CoraInstanceModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.retrieval.similarity.SimilarityFactory;
import services.retrieval.similarity.SimilarityFactoryImpl;
import services.retrieval.similarity.functions.SimilarityFunction;

import javax.naming.ConfigurationException;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by daniel on 31.08.14.
 */
public class InstanceSimilarityTest {

    CoraCaseBase caseBase;
    CoraCaseModel caseA;
    CoraCaseModel caseB;

    @Before
    public void testSetup() {
        CoraCaseBase caseBase = null;

        try {
            caseBase = new CoraCaseBaseImpl();
            this.caseBase = caseBase;
        } catch (ConfigurationException e) {
            fail(e.getMessage());
        } catch (FileNotFoundException e) {
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
    public void testCaseStructureOnly() {

        assertNotNull(caseA);
        assertNotNull(caseB);

        CoraWeightModel weights = new CoraWeightModel();
        CoraQueryModel query = new CoraQueryModel(caseA, weights, 10);

        SimilarityFactory simfac = new SimilarityFactoryImpl(query, caseBase);
        SimilarityFunction<CoraInstanceModel> simFunc = simfac.getFunction(CoraInstanceModel.class);
        assertNotNull(simFunc);

        System.out.println("Ähnlichkeit: " + simFunc.calculate(null, caseA.getCaseRoot(), caseB.getCaseRoot()));
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
