package models.ontology.assertions;

import com.hp.hpl.jena.rdf.model.Statement;
import models.ontology.CoraOntologyModelFactory;

/**
 * Created by daniel on 04.09.14.
 */
public abstract class Assertion<T, K, V> {

    private Statement statement;
    private CoraOntologyModelFactory factory;

    public Assertion(Statement statement) {
        this.statement = statement;
    }

    public abstract T getSubject();

    public abstract K getPredicat();

    public abstract V getObject();

    public Statement getBaseObject() {
        return statement;
    }

    public CoraOntologyModelFactory getFactory() {
        return factory;
    }

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
