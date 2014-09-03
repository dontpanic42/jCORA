package services.adaption;

import models.cbr.CoraCaseModel;
import services.adaption.rules.AdaptionRule;

/**
 * Created by daniel on 03.09.14.
 */
public interface AdaptionProgressHandler {
    public void onProgress(int max, int current, AdaptionRule lastRule);

    public void onFinish(CoraCaseModel result);

    public void onError(Throwable error, AdaptionRule triggeringRule);
}
