package models.ontology.datatypes;

import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import models.datatypes.TypedValue;
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
    public static final String MAPPING_PROPERTIES_FILE = "config/typemapping.properties";

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
                InputStream is = DatatypeMapper.class.getClassLoader().getResourceAsStream(MAPPING_PROPERTIES_FILE);
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

    /**
     * Gibt den Wert des Literals <code>l</code> als <code>TypedValue</code> zurück.
     * @param l Das Literal
     * @return TypedValue oder null im Fehlerfall.
     */
    public static TypedValue<?> getValue(Literal l) {
        String datatypeURI = l.getDatatypeURI();

        try {
            Class<? extends TypedValue<?>> clazz = (Class<? extends TypedValue<?>>) getMapping(datatypeURI);
            if(clazz == null) {
                return null;
            }

            TypedValue<?> value = clazz.newInstance();
            value.setFromLiteral(l);
            return value;
        } catch (ClassCastException e) {
            System.out.println("ClassCastException DatatypeURI: " + datatypeURI);
            e.printStackTrace();
        } catch (InstantiationException e) {
            System.out.println("InstantiationException DatatypeURI: " + datatypeURI);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.out.println("IllegalAccessException DatatypeURI: " + datatypeURI);
            e.printStackTrace();
        }

        return null;
    }
}
