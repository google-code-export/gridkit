package org.gridkit.nimble.scenario;

import java.util.ArrayList;
import java.util.List;

import org.gridkit.nimble.platform.Play;
import org.gridkit.nimble.util.FutureListener;
import org.gridkit.nimble.util.FutureOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;

public class SeqScenario implements Scenario {
    private static final Logger log = LoggerFactory.getLogger(SeqScenario.class);
    
    private final String name;
    private final List<Scenario> scenarios;
    
    public SeqScenario(String name, List<Scenario> scenarios) {
        this.name = name;
        this.scenarios = new ArrayList<Scenario>(scenarios);
    }

    public SeqScenario(List<Scenario> scenarios) {
        this(ScenarioOps.getCompositeName("Seq", scenarios), scenarios);
    }

    @Override
    public <T> Play<T> play(Context<T> context) {
        SeqPlay<T> play = new SeqPlay<T>(context);
        play.action();
        return play;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    private class SeqPlay<T> extends AbstractPlay<T> {
        private final SeqPipeline<T> pipeline;
        
        public SeqPlay(Context<T> context) {
            super(SeqScenario.this, context.getStatsFactory().emptyStats());
            this.pipeline = new SeqPipeline<T>(context);
        }
        
        public void action() {
            ScenarioOps.logStart(log, SeqScenario.this);
            pipeline.start(this);
        }
        
        @Override
        public ListenableFuture<Void> getCompletionFuture() {
            return pipeline;
        }
    }

    private class SeqPipeline<T> extends AbstractFuture<Void> implements FutureListener<Void> {
        private final List<Scenario> nextScenarios;

        private final Context<T> context;

        private AbstractPlay<T> play;
        private Play<T> curPlay;
        
        private ListenableFuture<Void> future;
        private final Object futureMonitor = new Object();

        public SeqPipeline(Context<T> context) {
            this.nextScenarios = scenarios;
            this.context = context;
        }
        
        public void start(AbstractPlay<T> play) {
            this.play = play;
            onSuccess(null);
        }

        @Override
        public void onSuccess(Void result) {
            if (curPlay != null) {
                play.update(new Runnable() {
                    @Override
                    public void run() {
                        play.setStats(context.getStatsFactory().combine(play.getStats(), curPlay.getStats()));
                        
                        if (curPlay.getStatus() == Play.Status.Failure) {
                            if (play.setStatus(Play.Status.Failure)) {
                                ScenarioOps.logFailure(log, SeqScenario.this, curPlay.getScenario());
                            }
                        } else if (curPlay.getStatus() != Play.Status.Success) {
                            if (play.setStatus(Play.Status.Failure)) {
                                ScenarioOps.logFailure(log, SeqScenario.this, curPlay.getScenario(), curPlay.getStatus());
                            }
                        } else if (nextScenarios.isEmpty()) {
                            if (play.setStatus(Play.Status.Success)) {
                                ScenarioOps.logSuccess(log, SeqScenario.this);
                            }
                        }
                    }
                });
            }
            
            if (!nextScenarios.isEmpty()) {
                synchronized (futureMonitor) {
                    if (isCancelled()) {
                        return;
                    }
                    
                    Scenario scenario = nextScenarios.remove(0);
                    
                    curPlay = scenario.play(context);
                    future = curPlay.getCompletionFuture();
                    
                    FutureOps.addListener(future, this, context.getExecutor());
                }
            } else {
                set(null);
            }
        }

        @Override
        public void onFailure(Throwable t, boolean afterSuccess, boolean afterCancel) {
            if (play.setStatus(Play.Status.Failure)) {
                ScenarioOps.logFailure(log, SeqScenario.this, t);
                setException(t);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {            
            if (isDone()) {
                return false;
            }
            
            synchronized (futureMonitor) {
                try {
                    future.cancel(mayInterruptIfRunning);
                } finally {
                    if (play.setStatus(Play.Status.Canceled)) {
                        ScenarioOps.logCancel(log, SeqScenario.this);
                    }
                }
                return super.cancel(false);
            }
        }
        
        @Override
        public void onCancel() {
            
        }
    }
    
    @Override
    public String toString() {
        return "SeqScenario[" + name + "]";
    }
}
