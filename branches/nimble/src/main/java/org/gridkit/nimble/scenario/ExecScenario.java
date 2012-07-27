package org.gridkit.nimble.scenario;

import static org.gridkit.nimble.util.StringOps.F;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import org.gridkit.nimble.platform.AttributeContext;
import org.gridkit.nimble.platform.LocalAgent;
import org.gridkit.nimble.platform.Play;
import org.gridkit.nimble.platform.Play.Status;
import org.gridkit.nimble.platform.RemoteAgent;
import org.gridkit.nimble.statistics.StatsFactory;
import org.gridkit.nimble.statistics.StatsProducer;
import org.gridkit.nimble.util.FutureListener;
import org.gridkit.nimble.util.FutureOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;

public class ExecScenario implements Scenario {
    private static final Logger log = LoggerFactory.getLogger(ExecScenario.class);
    
    public static interface Context<T> extends AttributeContext {
        StatsFactory<T> getStatsFactory();
        
        LocalAgent getLocalAgent();
    }
    
    public static interface Executable extends Serializable {
        public <T> Result<T> excute(Context<T> context) throws Exception;
    }
    
    public static final class Result<T> implements Serializable {
        private Play.Status status;
        private T stats;

        public Result(Status status, T stats) {
            this.status = status;
            this.stats = stats;
        }

        public Play.Status getStatus() {
            return status;
        }

        public void setStatus(Play.Status status) {
            this.status = status;
        }

        public T getStats() {
            return stats;
        }

        public void setStats(T stats) {
            this.stats = stats;
        }
    }
    
    private final Executable executable;
    
    private final String name;

    private final RemoteAgent agent;
    
    public ExecScenario(String name, Executable executable, RemoteAgent agent) {
        this.name = name;
        this.executable = executable;
        this.agent = agent;
    }
    
    public ExecScenario(Executable executable, RemoteAgent agent) {
        this(
            ScenarioOps.getName("Exec", Arrays.asList(executable.toString(), agent.toString())),
            executable, agent
        );
    }
    
    @Override
    public <T> Play<T> play(Scenario.Context<T> context) {
        ExecPlay<T> play = new ExecPlay<T>(this, context, agent);
        play.action();
        return play;
    }

    @Override
    public String getName() {
        return name;
    }
    
    private class ExecPlay<T> extends AbstractPlay<T> {
        private final ExecPipeline<T> pipeline;
        
        public ExecPlay(Scenario scenario, Scenario.Context<T> context, RemoteAgent agent) {
            super(scenario, context.getStatsFactory().emptyStats());
            pipeline = new ExecPipeline<T>(context);
        }
        
        public void action() {
            pipeline.start(this);
        }
        
        @Override
        public ListenableFuture<Void> getCompletionFuture() {
            return pipeline;
        }
    }
    
    private class ExecPipeline<T> extends AbstractFuture<Void> implements FutureListener<Result<T>> {
    	
        private final Scenario.Context<T> context;
        
        private volatile AbstractPlay<T> play;

        private volatile Future<Result<T>> future;
        
        public ExecPipeline(Scenario.Context<T> context) {
            this.context = context;
        }
        
        public void start(AbstractPlay<T> play) {
            ScenarioOps.logStart(log, ExecScenario.this);
            
            this.play = play;
            
            Executor<T> executor = new Executor<T>(
                context.getContextId(), executable, context.getStatsFactory()
            );
            
            future = agent.invoke(executor);
            
            FutureOps.addListener(DumbListenableFuture.wrap("Poller", future), this, context.getExecutor());
        }

        @Override
        public void onSuccess(final Result<T> result) {
            play.update(new Runnable() {
                @Override
                public void run() {
                    play.setStats(result.getStats());
                    
                    if (result.getStatus() == Play.Status.Failure) {
                        play.setStatus(Play.Status.Failure);
                        ScenarioOps.logFailure(log, ExecScenario.this, executable.toString());
                    } else if (result.getStatus() == Play.Status.Success) {
                        play.setStatus(Play.Status.Success);
                        ScenarioOps.logSuccess(log, ExecScenario.this);
                    } else {
                        play.setStatus(Play.Status.Failure);
                        ScenarioOps.logFailure(log, ExecScenario.this, executable.toString(), result.getStatus());
                    }
                    
                    set(null);
                }
            });
        }

        @Override
        public void onFailure(Throwable t, FailureEvent event) {
            if (play.setStatus(Play.Status.Failure)) {
                ScenarioOps.logFailure(log, ExecScenario.this, t);
                setException(t);
            }
        }
        
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (isDone()) {
                return false;
            }
            
            try {
                future.cancel(mayInterruptIfRunning);
            } finally {
                if (play.setStatus(Play.Status.Canceled)) {
                    ScenarioOps.logCancel(log, ExecScenario.this);
                }
            }

            return super.cancel(false);
        }

        @Override
        public void onCancel() {
            
        }
    }

    private static class Executor<T> implements RemoteAgent.Invocable<Result<T>>, Context<T> {
        private String contextId;
        private Executable executable;
        private StatsFactory<T> statsFactory;
        
        private transient LocalAgent agent;
        
        public Executor(String contextId, Executable executable, StatsFactory<T> statsFactory) {
            this.contextId = contextId;
            this.executable = executable;
            this.statsFactory = statsFactory;
        }

        @Override
        public Result<T> invoke(LocalAgent agent) {
            this.agent = agent;
            
            try {
                return executable.excute(this);
            } catch (Throwable t) {
                StatsProducer<T> producer = statsFactory.newStatsProducer();
                
                producer.report(
                    F("Exception during executable '%s' execution on agent '%s'", executable.toString()), 
                    agent.currentTimeMillis(), t
                );
                
                return new Result<T>(Play.Status.Failure, producer.produce());
            }
        }
        
        @Override
        public LocalAgent getLocalAgent() {
            return agent;
        }
        
        @Override
        public StatsFactory<T> getStatsFactory() {
            return statsFactory;
        }
        
        @Override //TODO implement attributes cleaning
        public ConcurrentMap<String, Object> getAttributesMap() {
            if (!agent.getAttributesMap().containsKey(contextId)) {
                agent.getAttributesMap().putIfAbsent(contextId, new ConcurrentHashMap<String, Object>());
            }

            @SuppressWarnings("unchecked")
            ConcurrentMap<String, Object> result = (ConcurrentMap<String, Object>)agent.getAttributesMap().get(contextId);
            
            return result;
        }
    }
    
    @Override
    public String toString() {
        return "ExecScenario[" + name + "]";
    }
}
