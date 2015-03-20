package models.ontology;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by daniel on 20.08.14.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        OntologyModelFactoryTest.class,
        ClassModelTest.class,
        InstanceModelTest.class,
        PropertyModelTest.class
})
public class OntologyModelTest {
}
