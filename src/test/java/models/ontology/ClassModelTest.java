package models.ontology;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import factories.ontology.CoraOntologyModelFactory;
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
public class ClassModelTest {

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
    public void testGetParent() {
        String namespace = domainModel.getNsPrefixURI("");
        CoraClassModel clazz;
        Set<CoraClassModel> parents;

        //Simple Class Test
        OntClass simpleClass = domainModel.getOntClass(namespace + "SimpleTestClass");
        assertNotNull(simpleClass);
        clazz = factory.wrapClass(simpleClass);
        parents = clazz.getFlattenedParents();
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
    }

    @Test
    public void testGetChildren() {
        String namespace = domainModel.getNsPrefixURI("");
        CoraClassModel clazz;
        Set<CoraClassModel> children;

        //Simple Class Test
        OntClass simpleClass = domainModel.getOntClass(namespace + "SimpleTestClass");
        assertNotNull(simpleClass);
        clazz = factory.wrapClass(simpleClass);
        children = clazz.getFlattenedChildren();
        OntClass cSubClass1 = domainModel.getOntClass(namespace + "SubClass1");
        assertTrue(children.size() == 1);
        assertTrue(children.contains(clazz.getFactory().wrapClass(cSubClass1)));

//        //Test And-Class
        OntClass andClass = domainModel.getOntClass(namespace + "AndClass2");
        clazz = factory.wrapClass(andClass);
        children = clazz.getFlattenedChildren();
        OntClass cAndTestClass = domainModel.getOntClass(namespace + "AndTestClass");
        assertTrue(children.size() == 1);
        assertTrue(children.contains(clazz.getFactory().wrapClass(cAndTestClass)));
    }

    @Test
    public void testGetInstances() {
        String namespace = domainModel.getNsPrefixURI("");
        CoraClassModel clazz;
        Set<CoraInstanceModel> children;

        OntClass andClass = domainModel.getOntClass(namespace + "AndClass2");
        clazz = factory.wrapClass(andClass);
        children = clazz.getInstances();
        assertTrue(children.size() == 2);


        Individual i1 = domainModel.getIndividual(namespace + "MultiAndTestIndividual");
        //AndTestIndividual hat AndTest als typ, damit auch AndClass2
        Individual i2 = domainModel.getIndividual(namespace + "AndTestIndividual");
        assertTrue(children.contains(factory.wrapInstance(i1)));
        assertTrue(children.contains(factory.wrapInstance(i2)));

    }

    @Test
    public void testGetProperties() {
        String namespace = domainModel.getNsPrefixURI("");
        CoraClassModel clazz;

        OntClass simpleClass = domainModel.getOntClass(namespace + "SimpleTestClass");
        clazz = factory.wrapClass(simpleClass);

        Set<CoraPropertyModel<?>> properties = clazz.getProperties();
        assertTrue(properties.size() == 1);
        OntProperty simpleProperty = domainModel.getOntProperty(namespace + "SimpleProperty");
        assertTrue(properties.contains(factory.wrapProperty(simpleProperty)));

        OntClass subClass = domainModel.getOntClass(namespace + "SubClass1");
        clazz = factory.wrapClass(subClass);
        properties = clazz.getProperties();
        assertTrue(properties.size() == 2);
        OntProperty superClassProperty = domainModel.getOntProperty(namespace + "SubClass1Property");
        assertTrue(properties.contains(factory.wrapProperty(simpleProperty)));
        assertTrue(properties.contains(factory.wrapProperty(superClassProperty)));
    }

    @After
    public void teardownTests() {
        domainModel.close();
    }
}
