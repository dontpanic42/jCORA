package models.ontology;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import factories.ontology.CoraOntologyModelFactory;
import models.datatypes.FloatValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;
/**
 * Created by daniel on 20.08.14.
 */
public class PropertyModelTest {

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
    public void testDataPropertyDataType() {
        String namespace = domainModel.getNsPrefixURI("");

        OntProperty prop = domainModel.getOntProperty(namespace + "TestFloatProperty");
        CoraPropertyModel model = factory.wrapProperty(prop);
        assertTrue(model.getRangeDataType() == FloatValue.class);

        prop = domainModel.getOntProperty(namespace + "SimpleProperty");
        model = factory.wrapProperty(prop);
        assertTrue(model.getRangeDataType() == CoraInstanceModel.class);
    }

    @After
    public void teardownTests() {
        domainModel.close();
    }

}
