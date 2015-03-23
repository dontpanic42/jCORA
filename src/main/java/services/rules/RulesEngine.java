package services.rules;

import models.cbr.CoraCaseModel;

/**
 * Created by daniel on 11.03.2015.
 *
 * Abstrakte Vorlage für eine RulesEngine.
 */
public abstract class RulesEngine {
    public abstract CoraCaseModel applyRuleset(CoraCaseModel baseCase, String param) throws Exception;
}
