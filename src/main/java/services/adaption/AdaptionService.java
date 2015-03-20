package services.adaption;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraRetrievalResult;
import riotcmd.trig;
import services.adaption.rules.AdaptionRule;
import services.adaption.rules.GlobalAdaptionRule;
import services.adaption.rules.LocalAdaptionRule;
import services.adaption.utility.PartialCaseCopier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by daniel on 03.09.14.
 *
 * Führt die Fall-Adaption durch
 */
public class AdaptionService {

    /**
     * Erzeugt einen Task, der die Anpassung durchführt.
     *
     * @param ruleStack
     * @param query
     * @param selectedCase
     * @param retrievalResults
     */
    public AdaptionTask createAdaptionTask(List<AdaptionRule> ruleStack,
                              CoraQueryModel query,
                              CoraCaseModel selectedCase,
                              List<CoraRetrievalResult> retrievalResults) {

        AdaptionTask task = new AdaptionTask();
        task.setRuleStack(ruleStack);
        task.setQuery(query);
        task.setSelectedCase(selectedCase);
        task.setRetrievalResults(retrievalResults);

        return task;
    }

    /**
     * Task für die serielle Adaption eines Falles
     */
    public class AdaptionTask extends Task<CoraCaseModel> {
        private List<AdaptionRule> ruleStack;
        private CoraQueryModel query;
        private CoraCaseModel selectedCase;
        private List<CoraRetrievalResult> retrievalResults;

        /**
         * Gibt die Regelmenge zurück
         * @return
         */
        public List<AdaptionRule> getRuleStack() {
            return ruleStack;
        }

        /**
         * Setzt die Regelmenge
         * @param ruleStack
         */
        public void setRuleStack(List<AdaptionRule> ruleStack) {
            this.ruleStack = ruleStack;
        }

        /**
         * Gibt die Nutzeranfrage ("neuer Fall") zurück
         * @return
         */
        public CoraQueryModel getQuery() {
            return query;
        }

        /**
         * Setzt die Nutzeranfrage ("neuer Fall")
         * @param query
         */
        public void setQuery(CoraQueryModel query) {
            this.query = query;
        }

        /**
         * Gibt den vom Nutzer ausgewählten Altfall für die Adaption zurück
         * @return Der vom Nutzer ausgewählte Altfall
         */
        public CoraCaseModel getSelectedCase() {
            return selectedCase;
        }

        /**
         * Setzt den vom Nutzer ausgewählten Altfall
         * @param selectedCase Der vom Nutzer ausgewählte Altfall
         */
        public void setSelectedCase(CoraCaseModel selectedCase) {
            this.selectedCase = selectedCase;
        }

        /**
         * Gibt die Ergebnismenge des Retrievals zurück. Globale Regeln können diese nutzen, um ggf. durchschnitte etc. zu berechnen.
         * @return Die Ergebnismenge
         */
        public List<CoraRetrievalResult> getRetrievalResults() {
            return retrievalResults;
        }

        /**
         * Setzt die Ergebnismenge des Retrievals. Globale Regeln können diese nutzen, um ggf. durchschnitte etc. zu berechnen.
         * @param retrievalResults Die Ergebnismenge
         */
        public void setRetrievalResults(List<CoraRetrievalResult> retrievalResults) {
            this.retrievalResults = retrievalResults;
        }

        /**
         * Führt die Adaption eines Falles anhand aller gegebenen Regeln durch
         * @return Der Adaptierte Fall
         * @throws Exception geworfen durch Regeln
         */
        @Override
        protected CoraCaseModel call() throws Exception {

            //Erzeuge eine Kopie
            //TODO: Dies sollte createWorkingCopy(getQuery().getCase()) sein. Dazu muss die Signatur der Adaptionsregeln angepasst werden
            //CoraCaseModel caseWorkingCopy = createWorkingCopy(getQuery().getCase());
            CoraCaseModel caseWorkingCopy = PartialCaseCopier.copyCaseDescription(getQuery().getCase(), null);


            if(ruleStack == null || ruleStack.size() == 0) {
                return caseWorkingCopy;
            }

            int counter = 0;
            for(AdaptionRule rule : ruleStack) {
                applyRule(caseWorkingCopy, getSelectedCase(), rule);

                updateProgress(counter++, ruleStack.size());
                updateMessage(rule.getRuleName());
            }

            return caseWorkingCopy;
        }

//        //TODO: Implement...
//        private CoraCaseModel createWorkingCopy(CoraCaseModel caseModel) {
//            return caseModel;
//        }

        /**
         * Führt die Adaption eines Falles anhand einer Regel durch
         * @param adaptedCase der zu adaptierende Fall
         * @param rule die Regel
         * @throws Exception geworfen durch die Regel
         */
        private void applyRule(CoraCaseModel adaptedCase, CoraCaseModel selectedCase, AdaptionRule rule)
                throws Exception {
            CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();

            if(rule.isGlobalAdaptionRule()) {
                GlobalAdaptionRule r = rule.asGlobalAdaptionRule();
                r.adapt(adaptedCase, getQuery(), selectedCase, getRetrievalResults(), caseBase);
                return;
            }

            if(rule.isLocalAdaptionRule()) {
                LocalAdaptionRule r = rule.asLocalAdaptionRule();
                r.adapt(adaptedCase, getQuery(), selectedCase);
                return;
            }
        }
    }
}
