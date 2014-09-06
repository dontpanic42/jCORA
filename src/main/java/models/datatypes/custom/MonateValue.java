package models.datatypes.custom;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import models.datatypes.IntegerValue;
import models.datatypes.custom.rdftypes.RDFCustomFloatType;
import models.datatypes.custom.rdftypes.RDFCustomIntegerType;

/**
 * Created by daniel on 05.09.14.
 */
public class MonateValue extends IntegerValue {
    private static RDFCustomIntegerType rdfType = null;
    private static final String TYPE_NS = "http://www.semanticweb.org/martin.kowalski/ontologies/2013/11/untitled-ontology-17#";
    private static final String TYPE_NAME = "Monate";

    @Override
    public Literal getLiteral(OntModel model) {
        if(rdfType == null) {
            rdfType = new RDFCustomIntegerType(TYPE_NS + TYPE_NAME);
            TypeMapper.getInstance().registerDatatype(rdfType);
        }

        return model.createTypedLiteral(getValue(), rdfType);
    }

    @Override
    public String getUnitName() {
        return TYPE_NAME;
    }
}
