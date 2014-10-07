package services.retrieval;

import models.cbr.CoraCaseBase;
import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import models.cbr.CoraRetrievalResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by daniel on 31.08.14.
 *
 * TODO: Diese Klasse als JavaFX.Concurrent.Task implementieren (Progress?).
 */
public abstract class RetrievalService implements Runnable {

    private Set<RetrievalProgressHandler> progressHandlers = new HashSet<>();
    private CoraCaseBase caseBase;
    private CoraQueryModel query;

    public void addProgressHandler(RetrievalProgressHandler handler) {
        progressHandlers.add(handler);
    }

    public void removeProgressHandler(RetrievalProgressHandler handler) {
        if(progressHandlers.contains(handler)) {
            progressHandlers.remove(handler);
        }
    }

    protected Set<RetrievalProgressHandler> getProgressHandlers() {
        return progressHandlers;
    }

    public CoraCaseBase getCaseBase() {
        return caseBase;
    }

    public void setCaseBase(CoraCaseBase caseBase) {
        this.caseBase = caseBase;
    }

    public CoraQueryModel getQuery() {
        return query;
    }

    public void setQuery(CoraQueryModel query) {
        this.query = query;
    }

    @Override
    public abstract void run();

    protected void notifyOnProgress(int max, int current, CoraQueryModel query, CoraCaseModel lastCase, Float lastSim, CoraCaseModel nextCase) {
        for(RetrievalProgressHandler h : getProgressHandlers()) {
            h.onProgress(max, current, query, lastCase, lastSim, nextCase);
        }
    }

    protected void notifyOnFinish(CoraQueryModel query, List<CoraRetrievalResult> results) {
        for(RetrievalProgressHandler h : getProgressHandlers()) {
            h.onFinish(query, results);
        }
    }

    protected void notifyOnError(CoraQueryModel query, Throwable error) {
        for(RetrievalProgressHandler h : getProgressHandlers()) {
            h.onError(query, error);
        }
    }
}
