package org.gridkit.nimble.platform;

import org.gridkit.nimble.scenario.Scenario;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class EmptyPlay<T> implements Play<T> {
    private final Scenario scenario;
    private final Play.Status status;
    private final T stats;
    private final ListenableFuture<Void> future;
    
    public EmptyPlay(Scenario scenario, Play.Status status, T stats) {
        this.scenario = scenario;
        this.status = status;
        this.stats = stats;
        this.future = Futures.immediateFuture(null);
    }

    @Override
    public Scenario getScenario() {
        return scenario;
    }

    @Override
    public T getStats() {
        return stats;
    }

    @Override
    public Play.Status getStatus() {
        return status;
    }

    @Override
    public ListenableFuture<Void> getCompletionFuture() {
        return future;
    }
}
