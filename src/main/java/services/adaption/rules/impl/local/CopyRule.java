package services.adaption.rules.impl.local;

import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import services.adaption.rules.LocalAdaptionRule;
import services.adaption.utility.PartialCaseCopier;
import view.viewbuilder.ViewBuilder;

/**
 * Created by daniel on 11.10.14.
 */
public class CopyRule extends LocalAdaptionRule {
    @Override
    public void adapt(CoraCaseModel adaptedCase, CoraQueryModel query, CoraCaseModel selectedCase) throws Exception {
        PartialCaseCopier.copyCaseSolution(selectedCase, adaptedCase);
    }

    @Override
    public String getRuleName() {
        return ViewBuilder.getInstance().getText("rule.copy_rule.name");
    }

    @Override
    public String getRuleDescription() {
        return ViewBuilder.getInstance().getText("rule.copy_rule.description");
    }
}
