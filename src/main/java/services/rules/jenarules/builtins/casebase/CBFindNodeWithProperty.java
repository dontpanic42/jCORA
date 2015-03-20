package services.rules.jenarules.builtins.casebase;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinException;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.Util;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;
import com.hp.hpl.jena.reasoner.rulesys.impl.BBRuleContext;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import services.rules.jenarules.builtins.util.CaseBaseUtils;

/**
 * Created by daniel on 13.03.2015.
 */
public class CBFindNodeWithProperty extends BaseBuiltin {
    @Override
    public String getName() {
        return "cbFindNodeWithProperty";
    }

    @Override
    public int getArgLength() {
        return 3;
    }

    @Override
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
        if(length < 3 || args.length < 3) {
            System.err.println("Builtin " + this.getClass().getSimpleName() + ": Not enough arguments.");
            return false;
        }

        Node property = getArg(0, args, context);
        System.out.println("Builtin " + this.getClass().getSimpleName() + ": Checking property: " + property);

        Node obj = getArg(1, args, context);
        System.out.println("Builtin " + this.getClass().getSimpleName() + ": Checking object: " + obj);

        Node i = findIndividual(property.toString(), obj.toString());
        if(i == null) {
            return false;
        }
        context.getEnv().bind(args[2], Node.createURI(i.getURI()));


        return true;
    }

    private Node findIndividual(String propertyName, String objectName) {
        return CaseBaseUtils.findIndividualWithProperty(propertyName, objectName);
    }
}
