package models.ontology.assertions;

import com.hp.hpl.jena.rdf.model.Statement;
import models.ontology.CoraOntologyModelFactory;

/**
 * Repräsentiert ein (Subjekt, Prädikat, Objekt)-Tripel
 * @param <T> Der Typ des Subjekts
 * @param <K> Der Typ des Prädikats
 * @param <V> Der Typ des Objekts
 */
public abstract class Assertion<T, K, V> {

    private Statement statement;
    private CoraOntologyModelFactory factory;

    /**
     * Konstruktor: Erzeugt eine neue <code>Assertion</code> von einem Jena-Objekt
     * @param statement
     */
    public Assertion(Statement statement) {
        this.statement = statement;
    }

    /**
     * Gibt das Subjekt des (Subjekt, Prädikat, Objekt)-Tripels zurück
     * @return Subjekt
     */
    public abstract T getSubject();

    /**
     * Gibt das Prädikate des (Subjekt, Prädikat, Objekt)-Tripels zurück.
     * @return Das Prädikat
     */
    public abstract K getPredicat();

    /**
     * Gibt das Objekt des (Subjekt, Prädikat, Objekt)-Tripels zurück.
     * @return Das Objekt
     */
    public abstract V getObject();

    /**
     * Gibt das (Subjekt, Prädikat, Objekt)-Tripel als Jena-Obejkt zurück.
     * @return Das Tripel als Jena-Objekt
     */
    public Statement getBaseObject() {
        return statement;
    }

    /**
     * Gibt die <code>CoraOntologyModelFactory</code> zurück, die dieses Tripel erzeugt hat.
     * @return Die Factory, die dieses Tripel erzeugt hat
     */
    public CoraOntologyModelFactory getFactory() {
        return factory;
    }

    /**
     * (Nur intern verwendet.) Setzt die <code>CoraOntologyModelFactory</code>.
     * @param factory Die Factory
     */
    public void setFactory(CoraOntologyModelFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assertion)) return false;

        Assertion that = (Assertion) o;

        if (statement != null ? !statement.equals(that.statement) : that.statement != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return statement != null ? statement.hashCode() : 0;
    }

    @Override
    public String toString() {
        String str = "Assertion " + getClass().getSimpleName() + ":\n";
        str += "Subjekt:\t" + getSubject() + "\n";
        str += "Prädikat:\t" + getPredicat() + "\n";
        str += "Objekt:\t\t" + getObject() + "\n";

        return str;
    }
}
