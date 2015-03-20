package models.ontology;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by daniel on 21.08.14.
 */
public class OntologyModelFactoryTest {

    private CoraOntologyModelFactory factory;
    private OntModel domainModel;

    @Before
    public void setupTests() {

        //Load the domain ontology

        OntModelSpec spec = new OntModelSpec(PelletReasonerFactory.THE_SPEC);

        domainModel = ModelFactory.createOntologyModel(spec);
        domainModel.read(FileManager.get().open("src/test/resources/classTestOntology.owl"), "RDF/XML");

        factory = new CoraOntologyModelFactory(domainModel, null);
    }

    @Test
    public void testModelEqualAndHash() {
        String ns = domainModel.getNsPrefixURI("");
        OntClass ontClass = domainModel.getOntClass(ns + "SubClass1");

        CoraClassModel cm1 = factory.wrapClass(ontClass);
        CoraClassModel cm2 = factory.wrapClass(ontClass);

        assertTrue(cm1.equals(cm2));
        assertTrue(cm1.hashCode() == cm2.hashCode());

        Set<CoraClassModel> set = new HashSet<CoraClassModel>();
        set.add(cm1);
        set.add(cm2);
        assertTrue(set.size() == 1);
        assertTrue(set.contains(cm1));
        assertTrue(set.contains(cm2));
    }

    @After
    public void teardownTests() {
        domainModel.close();
    }
}
