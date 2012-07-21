package org.gridkit.nimble.scenario;

import org.gridkit.nimble.platform.Play;

public abstract class AbstractPlay<T> implements Play<T> {
    protected final Scenario scenario;

    private Play.Status status;
    
    private T stats;

    public AbstractPlay(Scenario scenario, T initialStats) {
        this.scenario = scenario;
        this.stats = initialStats;
        this.status = Play.Status.InProgress;
    }

    @Override
    public Scenario getScenario() {
        return scenario;
    }

    @Override
    public synchronized T getStats() {
        return stats;
    }

    @Override
    public synchronized Play.Status getStatus() {
        return status;
    }
    
    protected synchronized void update(Runnable runnable) {
        runnable.run();
    }

    protected boolean setStatus(Play.Status status) {
        if (this.status == Play.Status.InProgress) {
            this.status = status;
            return true;
        }
        return false;
    }

    protected void setStats(T stats) {
        this.stats = stats;
    }
}
