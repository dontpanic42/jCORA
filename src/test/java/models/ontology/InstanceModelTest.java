package models.ontology;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import factories.ontology.CoraOntologyModelFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import static org.junit.Assert.*;

import java.util.Set;

/**
 * Created by daniel on 20.08.14.
 */
public class InstanceModelTest {

    private CoraOntologyModelFactory factory;
    private OntModel domainModel;

    @Before
    public void setupTests() {

        //Load the domain ontology

        OntModelSpec spec = new OntModelSpec(PelletReasonerFactory.THE_SPEC);

        domainModel = ModelFactory.createOntologyModel(spec);
        domainModel.read(FileManager.get().open("src/test/resources/classTestOntology.owl"), "RDF/XML");

        factory = new CoraOntologyModelFactory(domainModel);
    }

    @Test
    public void testGetFlattenedTypes() {

        String namespace = domainModel.getNsPrefixURI("");
        CoraInstanceModel inst;
        Set<CoraClassModel> parents;
        Individual i;

        i = domainModel.getIndividual(namespace + "AndTestIndividual");
        inst = factory.wrapInstance(i);
        parents = inst.getFlattenedTypes();
        assertTrue(parents.size() == 1);
        OntClass cAndTestClass = domainModel.getOntClass(namespace + "AndTestClass");
        assertTrue(parents.contains(factory.wrapClass(cAndTestClass)));

        i = domainModel.getIndividual(namespace + "MultiAndTestIndividual");
        inst = factory.wrapInstance(i);
        parents = inst.getFlattenedTypes();
        assertTrue(parents.size() == 2);
        OntClass cAndClass1 = domainModel.getOntClass(namespace + "AndClass1");
        OntClass cAndClass2 = domainModel.getOntClass(namespace + "AndClass2");
        assertTrue(parents.contains(factory.wrapClass(cAndClass1)));
        assertTrue(parents.contains(factory.wrapClass(cAndClass2)));
    }

    @After
    public void teardownTests() {
        domainModel.close();
    }
}
