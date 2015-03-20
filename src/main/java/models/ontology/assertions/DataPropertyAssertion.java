package models.ontology.assertions;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import models.datatypes.TypedValue;
import models.ontology.CoraDataPropertyModel;
import models.ontology.CoraInstanceModel;
import models.ontology.datatypes.DatatypeMapper;

/**
 * Created by daniel on 04.09.14.
 */
public class DataPropertyAssertion extends Assertion<CoraInstanceModel, CoraDataPropertyModel, TypedValue> {

    public DataPropertyAssertion(Statement s) {
        super(s);
    }

    @Override
    public CoraInstanceModel getSubject() {
        Statement s = getBaseObject();
        if(s.getSubject().canAs(Individual.class)) {
            return getFactory().wrapInstance(s.getSubject().as(Individual.class));
        }

        return null;
    }

    @Override
    public CoraDataPropertyModel getPredicat() {
        Statement s = getBaseObject();
        if(s.getPredicate().canAs(DatatypeProperty.class)) {
            return getFactory().wrapDataProperty(s.getPredicate().as(DatatypeProperty.class));
        }

        return null;
    }

    @Override
    public TypedValue getObject() {
        Statement s = getBaseObject();
        CoraDataPropertyModel property = getPredicat();
        Literal l = s.getObject().asLiteral();

        return DatatypeMapper.getTypedValue(property, l);
//        Class<? extends TypedValue> clazz;
//
//        try {
//            clazz = (Class<? extends TypedValue>) property.getRangeDataType();
//
//            if(clazz == null) {
//                return null;
//            }
//        } catch (ClassCastException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        TypedValue typedValue;
//        try {
//            typedValue = clazz.newInstance();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//            return null;
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        if(s.getObject().isLiteral()) {
//            typedValue.setFromLiteral(s.getObject().asLiteral());
//            return typedValue;
//        }
//
//        return null;
    }
}
