package org.gridkit.nimble.platform;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.gridkit.nimble.scenario.Scenario;
import org.gridkit.nimble.statistics.StatsFactory;
import org.gridkit.nimble.util.ValidOps;

public class Director<T> {
    private final Collection<RemoteAgent> agents;
    private final StatsFactory<T> statsFactory;
    private final ExecutorService executor;

    public Director(Collection<RemoteAgent> agents, StatsFactory<T> statsFactory, ExecutorService executor) {
        ValidOps.notEmpty(agents, "agents");
        ValidOps.notNull(statsFactory, "statsFactory");
        ValidOps.notNull(executor, "executor");
        
        this.agents = agents;
        this.statsFactory = statsFactory;
        this.executor = executor;
    }

    public Play<T> play(Scenario scenario) {
        ValidOps.notNull(scenario, "scenario");
        
        String id = scenario.getName() + "[" + UUID.randomUUID().toString() + "]";
        
        Scenario.Context<T> context = new DirectorContext(id);
        
        return scenario.play(context);
    }
    
    public void shutdown(boolean hard) {
        for (RemoteAgent agent : agents) {
            agent.shutdown(hard);
        }
        
        if (hard) {
            executor.shutdownNow();
        } else {
            executor.shutdown();
        }
    }
    
    private class DirectorContext implements Scenario.Context<T> {
        private final String id;

        public DirectorContext(String id) {
            this.id = id;
        }

        @Override
        public StatsFactory<T> getStatsFactory() {
            return statsFactory;
        }
        
        @Override
        public ExecutorService getExecutor() {
            return executor;
        }
        
        @Override
        public Collection<RemoteAgent> getAgents() {
            return agents;
        }

        @Override
        public String getContextId() {
            return id;
        }
    }
}
