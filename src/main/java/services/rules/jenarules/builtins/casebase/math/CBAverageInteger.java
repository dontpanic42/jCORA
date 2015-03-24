package services.rules.jenarules.builtins.casebase.math;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.Util;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;
import mainapp.MainApplication;
import models.cbr.CoraCaseBaseImpl;

/**
 * Created by daniel on 13.03.2015.
 *
 * Diese Klasse repräsentiert ein Builtin
 *      cbAvgInt(att, ?k)
 * wobei der durchschnittliche Wert des Attributs <code>att</code> in der
 * variablen ?k gespeichert wird (binding).
 *
 * Beispiel:
 *      cbAvgInt(ex:Mitarbeiteranzahl ?k)
 */
public class CBAverageInteger extends BaseBuiltin {
    @Override
    public String getName() {
        return "cbAvgInt";
    }

    @Override
    public int getArgLength() {
        return 2;
    }

    @Override
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
        if(length < 2 || args.length < 2) {
            System.err.println("Builtin " + this.getClass().getSimpleName() + ": Not enough arguments.");
            return false;
        }

        Node node = getArg(0, args, context);
        System.out.println("Builtin " + this.getClass().getSimpleName() + ": Checking " + node);

        int avg = getAverage(node);

        Node result = Util.makeIntNode(avg);
        context.getEnv().bind(args[1], result);

        return true;
    }

    private int getAverage(Node node) {
        String propName = node.toString();

        String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT (AVG(?value) as ?sum_value) \n" +
                "WHERE \n" +
                "  { \n" +
                "     GRAPH ?g {?y <" + propName + "> ?value}\n" +
                "  }";

        System.out.println("Query\n" + query);

        CoraCaseBaseImpl cbi = (CoraCaseBaseImpl) MainApplication.getInstance().getCaseBase();
        Dataset d = cbi.getDataset();

        d.begin(ReadWrite.READ);
        QueryExecution qe = QueryExecutionFactory.create(query, d);

        int result = 0;
        ResultSet resultSet = qe.execSelect();
        while(resultSet.hasNext()) {
            RDFNode solution = resultSet.next().get("sum_value");
            if(solution != null) {
                result = solution.asLiteral().getInt();
                System.out.println("Ergebnis: " + result);
            }
        }

        d.end();

        return result;
    }
}
