package services.rules;

import models.cbr.CoraCaseModel;

/**
 * Abstrakte Vorlage für eine RulesEngine.
 *
 * Created by daniel on 11.03.2015.
 */
public abstract class RulesEngine {
    public abstract CoraCaseModel applyRuleset(CoraCaseModel baseCase, String param) throws Exception;
}
