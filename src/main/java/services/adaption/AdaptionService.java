package services.adaption;

import mainapp.MainApplication;
import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraRetrievalResult;
import riotcmd.trig;
import services.adaption.rules.AdaptionRule;
import services.adaption.rules.GlobalAdaptionRule;
import services.adaption.rules.LocalAdaptionRule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by daniel on 03.09.14.
 *
 * Führt die Fall-Adaption durch
 */
public class AdaptionService {

    private Set<AdaptionProgressHandler> handlers = new HashSet<>();

    /**
     * Führt die Adaption des Falles auf Grundlage einer geordneten Liste von Adaptionsregeln durch.
     * Diese Methode ist asynchron (non-blocking).
     *
     * TODO: Kopiere den Fall aus der Query und gebe die Kopie an die Adaptionsregeln. Sonst steht der Nutzer im Fehlerfall ohne query da^^
     *
     * @param ruleStack
     * @param query
     * @param selectedCase
     * @param retrievalResults
     */
    public void startAdaption(List<AdaptionRule> ruleStack,
                              CoraQueryModel query,
                              CoraCaseModel selectedCase,
                              List<CoraRetrievalResult> retrievalResults) {
        new Thread(() -> {

            LocalAdaptionRule localRule;
            GlobalAdaptionRule globalRule;
            int numRules = ruleStack.size();
            int currentRuleIndex = 0;
            CoraCaseBase caseBase = MainApplication.getInstance().getCaseBase();

            for(AdaptionRule rule : ruleStack) {

                try {
                    if(rule.isGlobalAdaptionRule()) {
                        globalRule = rule.asGlobalAdaptionRule();

                        globalRule.adapt(query, selectedCase, retrievalResults, caseBase);

                        notifyOnProgress(numRules, currentRuleIndex, globalRule);
                    } else {
                        localRule = rule.asLocalAdaptionRule();

                        localRule.adapt(query, selectedCase);

                        notifyOnProgress(numRules, currentRuleIndex, localRule);
                    }
                } catch (Throwable e) {
                    notifyOnError(e, rule);
                    //Breche die gesamte Adaption ab, da diese (unvollständige) regel möglicherweise von einer anderen
                    //später bearbeiteten Regel abhängt.
                    return;
                }

                currentRuleIndex++;
            }

            notifyOnFinish(query.getCase());

        }).start();
    }

    /**
     * Fügt dem Service einen Progress-Handler hinzu
     * @param handler Der Progress-Handler
     */
    public void addProgressHandler(AdaptionProgressHandler handler) {
        handlers.add(handler);
    }

    /**
     * Entfernt einen Progresshandler
     * @param handler Der zu entfernende Progress-Handler
     */
    public void removeProgressHandler(AdaptionProgressHandler handler) {
        if(handlers.contains(handler)) {
            handlers.remove(handler);
        }
    }

    private void notifyOnError(Throwable error, AdaptionRule triggeringRule) {
        for(AdaptionProgressHandler h : handlers) {
            h.onError(error, triggeringRule);
        }
    }

    private void notifyOnFinish(CoraCaseModel caseModel) {
        for(AdaptionProgressHandler h : handlers) {
            h.onFinish(caseModel);
        }
    }

    private void notifyOnProgress(int max, int current, AdaptionRule lastRule) {
        for(AdaptionProgressHandler h : handlers) {
            h.onProgress(max, current, lastRule);
        }
    }
}
