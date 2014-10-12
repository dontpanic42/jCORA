package services.adaption.rules.impl.local;

import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import services.adaption.rules.LocalAdaptionRule;
import services.adaption.utility.PartialCaseCopier;

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
        return "Kopiere Lösung";
    }

    @Override
    public String getRuleDescription() {
        return "Kopiert die Lösung des ausgewählten Falles.";
    }
}
