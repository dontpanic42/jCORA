package models.ontology.datatypes;

import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import models.datatypes.TypedValue;
import models.ontology.CoraDataPropertyModel;
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
     * @return <code>Properties</code>-Objekt, das die Mappings enthält
     */
    private static Properties getMappings() {
        if(mapping == null) {
            mapping = new Properties();
            try {
                InputStream is = DatatypeMapper.class.getClassLoader().getResourceAsStream(MAPPING_PROPERTIES_FILE);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
                mapping.load(reader);
                reader.close();
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
                return DatatypeMapper.class.getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                System.err.println("Unbekante Datentyp-Klasse: " + className);
                return null;
            }
        }

        return null;
    }

    /**
     * Gibt den Java-Typ für eine <code>OntResource</code> zurück.
     * @see models.ontology.CoraDataPropertyModel#getRangeDataType()
     * @param r Die OntResource, deren URI den Typ beschreibt
     * @return Den Java-Typ oder <code>null</code>, wenn dieser unbekannt ist
     */
    public static Class<?> getMapping(OntResource r) {
        return getMapping(r.getURI());
    }

    /**
     * Gibt den Wert eines Literals unter Berücksichtigung des vorgegebenen typs
     * aus der DataProperty-Range zurück
     * @param property Attribut, das das Literal als Wert enthält
     * @param l Der Wert als Literal
     * @return Der Wert des Literals als <code>TypedValue</code>
     */
    public static TypedValue<?> getTypedValue(CoraDataPropertyModel property, Literal l) {
        Class<TypedValue<?>> tv;
        try {
            tv = (Class<TypedValue<?>>) property.getRangeDataType();
        } catch (ClassCastException e) {
            e.printStackTrace();
            tv = null;
        }

        //Wenn das Property keine range definiert hat, oder die Range unbekannt ist,
        //gib das literal als xsd-typ zurück
        if(tv == null) {
            return getDefaultValue(l);
        }

        try {
            TypedValue<?> value = tv.newInstance();
            value.setFromLiteral(l);
            return value;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gibt den Wert des Literals <code>l</code> als <code>TypedValue</code> zurück.
     * Dabei wird nur der Typ des Literals beachtet - das Ergebniss kann also nur ein
     * TypedValue für einen xsd-typ sein.
     * @param l Das Literal
     * @return TypedValue oder null im Fehlerfall.
     */
    public static TypedValue<?> getDefaultValue(Literal l) {
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
