package services.cls;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import mainapp.MainApplication;
import services.cls.parser.Evaluator;
import services.cls.parser.EvaluatorException;
import services.cls.parser.util.Node;

import java.util.List;

/**
 * Created by daniel on 15.05.15.
 */
public class ClassBuilderEvaluator extends Evaluator<OntResource> {

    private final OntModel model;

    public ClassBuilderEvaluator(OntModel model) {
        super();
        this.model = model;
    }

    @Override
    public OntResource evaluateExpression(Node node) throws EvaluatorException {
        final String NS = MainApplication.getInstance().getCaseBase().getDomainNs();

        String className = NS + node.getIdentifier();

        OntClass c = model.getOntClass(className);

        if(c == null) {
            throw new EvaluatorException("Konzept " + className + " nicht gefunden.");
        }

        return c;
    }

    @Override
    public OntResource evaluateOperation(Node operator, List<OntResource> arguments) throws EvaluatorException {
        final String opid = operator.getIdentifier().toLowerCase();

        RDFNode[] nodes = argumentsAsRDFNodeArray(arguments);
        RDFList list = model.createList(nodes);

        if(opid.equals("und")) {
            return model.createIntersectionClass(null, list);
        }

        if(opid.equals("oder")) {
            return model.createUnionClass(null, list);
        }

        throw new EvaluatorException("Unsupported operator: " + operator.getIdentifier());
    }

    /**
     * Gibt eine Liste von <code>OntResource</code>s als
     * RDFNode-Array zurück.
     * @param args Liste von <code>OntResouce</code>s
     * @return RDFNode-Array
     */
    private RDFNode[] argumentsAsRDFNodeArray(List<OntResource> args) {
        RDFNode[] rdf = new RDFNode[args.size()];
        for(int i = 0; i < args.size(); i++) {
            rdf[i] = (RDFNode) args.get(i);
        }

        return rdf;
    }
}
