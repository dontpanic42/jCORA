package services.adaption;

import services.adaption.rules.AdaptionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 03.09.14.
 */
public class AdaptionRuleFactory {

    public static List<AdaptionRule> rules = new ArrayList<AdaptionRule>() {
        {}
    };

    public List<AdaptionRule> getAvailableRules() {
        return rules;
    }
}
