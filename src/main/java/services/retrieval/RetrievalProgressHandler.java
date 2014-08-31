package services.retrieval;

import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraRetrievalResult;

import java.util.List;

/**
 * Created by daniel on 31.08.14.
 */
public interface RetrievalProgressHandler {
    public void onProgress(int max, int current, CoraQueryModel query, CoraCaseModel lastCase, Float lastSim, CoraCaseModel nextCase);

    public void onFinish(CoraQueryModel query, List<CoraRetrievalResult> results);

    public void onError(CoraQueryModel query, Throwable error);
}
