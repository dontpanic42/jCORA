package models.ontology.datatypes;

import com.hp.hpl.jena.ontology.OntResource;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.Properties;

/**
 * Created by daniel on 20.08.14.
 */
public class DatatypeMapper {

    /**
     * 'properties' Datei, die die Typemappings enthält.
     */
    public static final String MAPPING_PROPERTIES_FILE = "src/main/config/typemapping.properties";

    /**
     * Gecachte version der mappings
     */
    static Properties mapping = null;

    /**
     * Lädt die Datentyp-Mappings aus der <code>MAPPING_PROPERTIES_FILE</code>-Datei, falls dies
     * bisher noch nicht geschehen ist.
     * @return
     */
    private static Properties getMappings() {
        if(mapping == null) {
            mapping = new Properties();
            try {
                File propertyFile = new File(MAPPING_PROPERTIES_FILE);
                InputStream is = new FileInputStream(propertyFile);
                mapping.load(is);
                is.close();
            } catch (IOException e) {
                System.err.println("Keine Mapping-Datei: " + MAPPING_PROPERTIES_FILE);
                throw new NotImplementedException();
            }
        }

        return mapping;
    }

    /**
     * Gibt den Java-Typ für eine (xsd-)URI zurück.
     * @param uri URI, die den Datentyp beschreibt
     * @return Den Java-Typ oder <code>null</code>, wenn dieser unbekannt ist
     */
    public static Class<?> getMapping(String uri) {

        Properties map = getMappings();

        if(map.containsKey(uri)) {
            String className = (String) map.get(uri);

            try {
                Class<?> clazz = DatatypeMapper.class.getClassLoader().loadClass(className);
                return clazz;
            } catch (ClassNotFoundException e) {
                System.err.println("Unbekante Datentyp-Klasse: " + className);
                return null;
            }
        }

        return null;
    }

    /**
     * Gibt den Java-Typ für eine <code>OntResource</code> zurück.
     * @param r Die OntResource, deren URI den Typ beschreibt
     * @return Den Java-Typ oder <code>null</code>, wenn dieser unbekannt ist
     */
    public static Class<?> getMapping(OntResource r) {
        return getMapping(r.getURI());
    }
}
