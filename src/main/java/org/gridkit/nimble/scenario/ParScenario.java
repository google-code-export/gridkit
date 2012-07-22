package org.gridkit.nimble.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.gridkit.nimble.platform.Play;
import org.gridkit.nimble.util.FutureListener;
import org.gridkit.nimble.util.FutureOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class ParScenario implements Scenario {
    private static final Logger log = LoggerFactory.getLogger(ParScenario.class);
    
    private final String name;
    private final List<Scenario> scenarios;
    private final boolean interruptOnFailure;
    
    public ParScenario(String name, List<Scenario> scenarios, boolean interruptOnFailure) {
        this.name = name;
        this.scenarios = scenarios;
        this.interruptOnFailure = interruptOnFailure;
    }
    
    public ParScenario(String name, List<Scenario> scenarios) {
        this(name, scenarios, true);
    }
    
    public ParScenario(List<Scenario> scenarios) {
        this(ScenarioOps.getCompositeName("Par", scenarios), scenarios);
    }

    @Override
    public <T> Play<T> play(Context<T> context) {
        ParPlay<T> play = new ParPlay<T>(context);
        
        play.registerCallbacks();
        
        return play;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    private class ParPlay<T> extends AbstractPlay<T> implements FutureListener<Play<T>> {
        private final Context<T> context;
        
        private final AtomicInteger playsRemain;
        
        private final List<ListenableFuture<Play<T>>> playFutures;
        
        private final ParFuture<T> future;

        public ParPlay(Context<T> context) {
            super(ParScenario.this, context.getStatsFactory().emptyStats());

            ScenarioOps.logStart(log, ParScenario.this);
            
            this.context = context;
            
            this.playsRemain = new AtomicInteger(scenarios.size());
            
            this.playFutures = new ArrayList<ListenableFuture<Play<T>>>(scenarios.size());
            
            for (Scenario scenario : scenarios) {
                Play<T> play = scenario.play(context);
                
                ListenableFuture<Play<T>> playFuture = Futures.transform(
                    play.getCompletionFuture(), Functions.constant(play)
                );
                
                playFutures.add(playFuture);
            }
            
            this.future = new ParFuture<T>(this);
        }
        
        public void registerCallbacks() {
            for (ListenableFuture<Play<T>> playFuture : playFutures) {
                FutureOps.addListener(playFuture, this, context.getExecutor());
            }
        }
        
        public void cancel() {            
            for (ListenableFuture<Play<T>> playFuture : playFutures) {
                playFuture.cancel(interruptOnFailure);
            }
        }
        
        @Override
        public ListenableFuture<Void> getCompletionFuture() {
            return future;
        }

        @Override
        public void onSuccess(final Play<T> play) {
            final int playsRemain = this.playsRemain.decrementAndGet();

            this.update(new Runnable() {
                @Override
                public void run() {
                    ParPlay.this.setStats(context.getStatsFactory().combine(ParPlay.this.getStats(), play.getStats()));
                    
                    if (play.getStatus() != Play.Status.Success) {
                        try {
                            cancel();
                        } finally {
                            ParPlay.this.setStatus(Play.Status.Failure);
                            future.set(null);
                        }
                        
                        if (play.getStatus() == Play.Status.Failure) {
                            ScenarioOps.logFailure(log, ParScenario.this, play.getScenario());
                        } else {
                            ScenarioOps.logFailure(log, ParScenario.this, play.getScenario(), play.getStatus());
                        }
                    } else if (playsRemain == 0) {
                        ParPlay.this.setStatus(Play.Status.Success);
                        ScenarioOps.logSuccess(log, ParScenario.this);
                        future.set(null);
                    }
                }
            });
        }

        // TODO combine statistics on failure
        @Override
        public void onFailure(Throwable t, boolean afterSuccess, boolean afterCancel) {
            try {
                cancel();
            } finally {
                if (setStatus(Play.Status.Failure)) {
                    ScenarioOps.logFailure(log, ParScenario.this, t);
                    future.setException(t);
                }
            }
        }

        @Override
        public void onCancel() {
            
        }
    }
    
    private class ParFuture<T> extends AbstractFuture<Void> {
        private final ParPlay<T> play;

        public ParFuture(ParPlay<T> play) {
            this.play = play;
        }

        public boolean set(Void value) {
            return super.set(value);
        };
        
        public boolean setException(Throwable throwable) {
            return super.setException(throwable);
        };
        
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (isDone()) {
                return false;
            }
            
            try {
                play.cancel();
            } finally {
                if (play.setStatus(Play.Status.Canceled)) {
                    ScenarioOps.logCancel(log, ParScenario.this);
                }
            }
            
            return super.cancel(false);
        };
    }
    
    @Override
    public String toString() {
        return "ParScenario[" + name + "]";
    }
}
