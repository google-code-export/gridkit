package org.gridkit.nimble.scenario;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.gridkit.nimble.platform.Play;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class SeqScenario implements Scenario {
    private static final Logger log = LoggerFactory.getLogger(SeqScenario.class);
    
    private final String name;
    private final List<Scenario> scenarios;
    
    public SeqScenario(String name, List<Scenario> scenarios) {
        this.name = name;
        this.scenarios = new ArrayList<Scenario>(scenarios);
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
            ScenarioLogging.logStart(log, SeqScenario.this);
            pipeline.start(this);
        }
        
        @Override
        public ListenableFuture<Void> getCompletionFuture() {
            return pipeline;
        }
    }

    private class SeqPipeline<T> extends AbstractFuture<Void> implements Runnable {
        private final List<Scenario> nextScenarios;

        private final Context<T> context;

        private volatile AbstractPlay<T> play;

        private ListenableFuture<Void> future = Futures.immediateFuture(null);
        private final Object futureMonitor = new Object();

        public SeqPipeline(Context<T> context) {
            this.nextScenarios = scenarios;
            this.context = context;
        }
        
        public void start(AbstractPlay<T> play) {
            this.play = play;
            run();
        }
        
        @Override
        public void run() {
            synchronized(futureMonitor) {
                if (!future.isCancelled()) {
                    runInternal();
                }
            }
        }
        
        public void runInternal() {
            Scenario scenario = nextScenarios.remove(0);

            final Play<T> curPlay = scenario.play(context);
            
            final ListenableFuture<Void> curFuture = curPlay.getCompletionFuture();

            SeqPipeline.this.future = curFuture;
            
            Futures.addCallback(curFuture, new FutureCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    play.update(new Runnable() {
                        @Override
                        public void run() {
                            if (curPlay.getStatus() == Play.Status.Failure) {
                                play.status = Play.Status.Failure;
                                ScenarioLogging.logFailure(log, SeqScenario.this, curPlay.getScenario());
                            } else if (curPlay.getStatus() != Play.Status.Success) {
                                play.status = Play.Status.Failure;
                                ScenarioLogging.logFailure(log, SeqScenario.this, curPlay.getScenario(), curPlay.getStatus());
                            }

                            play.stats = context.getStatsFactory().combine(play.stats, curPlay.getStats());
                        }
                    });

                    if (play.getStatus() == Play.Status.Failure) {
                        set(null);
                    } else if (nextScenarios.isEmpty()) {
                        ScenarioLogging.logSuccess(log, SeqScenario.this);
                        play.setStatus(Play.Status.Success);
                        set(null);
                    } else {
                        run();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    if (!curFuture.isCancelled()) {
                        play.setStatus(Play.Status.Failure);
                        ScenarioLogging.logFailure(log, SeqScenario.this, t);
                        setException(t);
                    }
                }
            }, context.getExecutor());
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {            
            if (isDone()) {
                return false;
            }
            
            boolean justCancelled;
            synchronized (futureMonitor) {
                justCancelled = future.cancel(mayInterruptIfRunning);
            }
            
            if (justCancelled) {
                play.setStatus(Play.Status.Canceled);
                ScenarioLogging.logCancel(log, SeqScenario.this);
                super.cancel(false);
            }
            
            return justCancelled;
        }
    }
    
    @Override
    public String toString() {
        return "SeqScenario[" + name + "]";
    }
}
