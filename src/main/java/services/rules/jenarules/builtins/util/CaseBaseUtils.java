package services.rules.jenarules.builtins.util;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import mainapp.MainApplication;
import models.cbr.CoraCaseBaseImpl;

/**
 * Created by daniel on 13.03.2015.
 */
public class CaseBaseUtils {
    public static Node findIndividualWithProperty(String propertyName, String objectName) {

        String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT ?no \n" +
                "WHERE \n" +
                "  { \n" +
                "     GRAPH ?g {?no <" + propertyName + "> <" + objectName + ">}\n" +
                "  }";

        System.out.println("Query\n" + query);

        CoraCaseBaseImpl cbi = (CoraCaseBaseImpl) MainApplication.getInstance().getCaseBase();
        Dataset d = cbi.getDataset();

        d.begin(ReadWrite.READ);
        QueryExecution qe = QueryExecutionFactory.create(query, d);

        int result = 0;
        ResultSet resultSet = qe.execSelect();
        while(resultSet.hasNext()) {
            RDFNode solution = resultSet.next().get("no");
            if(solution != null) {

                d.end();
                return solution.asNode();
            }
        }

        d.end();
        return null;
    }
}
