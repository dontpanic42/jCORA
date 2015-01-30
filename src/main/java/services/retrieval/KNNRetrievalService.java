package services.retrieval;

import com.sun.javaws.exceptions.InvalidArgumentException;
import models.cbr.CoraCaseModel;
import models.cbr.CoraRetrievalResult;
import models.ontology.CoraInstanceModel;
import services.retrieval.similarity.SimilarityFactory;
import services.retrieval.similarity.SimilarityFactoryImpl;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by daniel on 31.08.14.
 */
public class KNNRetrievalService extends RetrievalService {

    @Override
    public void run() {
        if(getQuery() == null || getCaseBase() == null) {
            notifyOnError(getQuery(), new Exception("Query oder CaseBase fehlen."));
            return;
        }

        RetrievalResults results = new RetrievalResults(getQuery().getMaxCases());

        CoraCaseModel last = null;
        CoraCaseModel next;
        Float simLast = null;
        List<String> cases = getCaseBase().getCaseIDs();
        for(String caseId : cases) {
            System.out.println("***************************************************************");
            System.out.println("Ähnlichkeitsberechnung Query/" + caseId);
            System.out.println("***************************************************************");
            try {
                SimilarityFactory simFac = new SimilarityFactoryImpl(getQuery(), getCaseBase());
                next = getCaseBase().loadCase(caseId);

                notifyOnProgress(0, 0, getQuery(), last, simLast, next);
                if(last != null) {
                    last.close();
                }

                simLast = simFac.getFunction(CoraInstanceModel.class).calculate(null,
                        getQuery().getCase().getCaseDescription(), next.getCaseDescription());

                last = next;

                CoraRetrievalResult result = new CoraRetrievalResult(next.getCaseId(), simLast);

                results.add(result);

            } catch(Throwable e) {
                notifyOnError(getQuery(), e);
                return;
            }
        }

        if(last != null) {
            notifyOnProgress(0, 0, getQuery(), last, simLast, null);
            last.close();
        }

        System.out.println(cases.size() + "Fälle durchsucht.");
        notifyOnFinish(getQuery(), results.asList());
    }


    private class RetrievalResults {
        private List<CoraRetrievalResult> list = new ArrayList<>();
        private int maxK;

        public RetrievalResults(int maxK) {
            this.maxK = maxK;
        }

        public void add(CoraRetrievalResult result) {
            list.add(result);
            Collections.sort(list);
            if(list.size() > maxK) {
                CoraRetrievalResult toRemove = list.get(0);
                list.remove(toRemove);
            }
        }

        public List<CoraRetrievalResult> asList() {
            Collections.reverse(list);
            return list;
        }
    }
}
