package services.adaption.rules;

import models.cbr.CoraCaseModel;
import models.cbr.CoraQueryModel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by daniel on 03.09.14.
 */
public abstract class LocalAdaptionRule implements AdaptionRule {
    @Override
    public boolean isLocalAdaptionRule() {
        return true;
    }

    @Override
    public boolean isGlobalAdaptionRule() {
        return false;
    }

    @Override
    public LocalAdaptionRule asLocalAdaptionRule() {
        return this;
    }

    @Override
    public GlobalAdaptionRule asGlobalAdaptionRule() {
        throw new NotImplementedException();
    }

    /**
     * Adaptiert deinen Fall.
     * @param query Die Query
     * @param selectedCase Der vom Nutzer ausgewählte, ähnliche Fall
     * @return
     */
    public abstract void adapt(CoraQueryModel query,
                               CoraCaseModel selectedCase)
            throws Throwable;
}
