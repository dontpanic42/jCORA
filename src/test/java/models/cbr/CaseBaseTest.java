package models.cbr;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.ReadWrite;
import factories.ontology.CoraOntologyModelFactory;
import models.ontology.CoraClassModel;
import models.ontology.CoraInstanceModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by daniel on 22.08.14.
 */
public class CaseBaseTest {

    CoraCaseBaseImpl caseBase;

    @Before
    public void createCaseBase() {
        try {
            File config = new File("src/test/resources/casebase.properties");
            caseBase = new CoraCaseBaseImpl();
        } catch (ConfigurationException e) {
            e.printStackTrace();
            Assert.fail("Unexpected ConfigurationException." + e.getMessage());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Unexpected FileNotFoundException." + e.getMessage());
        }
    }

    @Test
    public void testCaseIO() {

        CoraCaseModel m = null;
        try {
            m = caseBase.loadCase("someModel");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Assert.fail();
        }

        CoraInstanceModel caseInstance = m.getCaseRoot();
        assertNotNull(caseInstance);

        OntModel caseModel = caseInstance.getModel();

        //Testen, ob die domain-ontologie vorhanden ist...
        String domainNS = caseBase.getDomainModel().getNsPrefixURI("");
        OntClass testClass = caseModel.getOntClass(domainNS + "SimpleTestClass");
        assertNotNull(testClass);

        Individual i = caseModel.getIndividual("http://example.com/case/#TestIndividual");
        assertNotNull(i);

        caseModel.close();


    }

    @Test
    public void testClassInferenceOnPersistentModel() {

        //OntModel domainModel = caseBase.loadModel("someModel");

        CoraCaseModel m = null;
        try {
            m = caseBase.loadCase("someModel");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Assert.fail();
        }

        CoraInstanceModel caseInstance = m.getCaseRoot();
        assertNotNull(caseInstance);

        OntModel domainModel = caseInstance.getModel();

        String namespace = caseBase.getDomainModel().getNsPrefixURI("");

        CoraClassModel clazz;
        CoraOntologyModelFactory factory = new CoraOntologyModelFactory(domainModel);
        Set<CoraClassModel> parents;

        //Simple Class Test
        OntClass simpleClass = domainModel.getOntClass(namespace + "SimpleTestClass");
        assertNotNull(simpleClass);
        clazz = factory.wrapClass(simpleClass);
        parents = clazz.getFlattenedParents();
        System.out.println("ParentCount: " + parents.size());
        for(CoraClassModel c : parents) {
            System.out.println("Hat Parent: " + c.getBaseObject().getNameSpace() + c.getBaseObject().getLocalName());
        }

        assertTrue(parents.size() == 1);
        OntClass cSuperClass2 = domainModel.getOntClass(namespace + "SuperClass2");
        assertTrue(parents.contains(clazz.getFactory().wrapClass(cSuperClass2)));

//        //Test And-Class
        OntClass andClass = domainModel.getOntClass(namespace + "AndTestClass");
        clazz = factory.wrapClass(andClass);
        parents = clazz.getFlattenedParents();
        assertTrue(parents.size() == 2);
        OntClass cAndClass1 = domainModel.getOntClass(namespace + "AndClass1");
        OntClass cAndClass2 = domainModel.getOntClass(namespace + "AndClass2");
        assertTrue(parents.contains(clazz.getFactory().wrapClass(cAndClass1)));
        assertTrue(parents.contains(clazz.getFactory().wrapClass(cAndClass2)));

        OntClass orClass = domainModel.getOntClass(namespace + "OrClass");
        clazz = factory.wrapClass(orClass);
        parents = clazz.getFlattenedParents();
        OntClass cAndSuperClass = domainModel.getOntClass(namespace + "AndSuperClass");
        assertTrue(parents.size() == 1);
        assertTrue(parents.contains(clazz.getFactory().wrapClass(cAndSuperClass)));

        OntClass complex = domainModel.getOntClass(namespace + "ComplexClass");
        clazz = factory.wrapClass(complex);
        parents = clazz.getFlattenedParents();
        assertTrue(parents.size() == 1); // Thing...

        m.close();
    }

    @After
    public void closeCaseBase() {
        if(caseBase != null) {
            caseBase.close();
        }
    }
}
