package org.gridkit.nimble.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.gridkit.nimble.platform.Play;
import com.google.common.base.Functions;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FutureCallback;
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
    
    private class ParPlay<T> extends AbstractPlay<T> implements FutureCallback<Play<T>> {
        private final Context<T> context;
        
        private final AtomicInteger playsRemain;
        
        private final List<ListenableFuture<Play<T>>> playFutures;
        
        private final ParFuture<T> future;

        public ParPlay(Context<T> context) {
            super(ParScenario.this, context.getStatsFactory().emptyStats());

            ScenarioLogging.logStart(log, ParScenario.this);
            
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
                Futures.addCallback(playFuture, this, context.getExecutor());
            }
        }
        
        public synchronized boolean cancel() {            
            boolean result = false;
                        
            for (ListenableFuture<Play<T>> playFuture : playFutures) {
                boolean playCanceled = playFuture.cancel(interruptOnFailure); // do not insert into result expression
                
                result = result || playCanceled;
            }

            return result;
        }
        
        @Override
        public ListenableFuture<Void> getCompletionFuture() {
            return future;
        }

        @Override
        public void onSuccess(final Play<T> paly) {
            final int playsRemain = this.playsRemain.decrementAndGet();

            this.update(new Runnable() {
                @Override
                public void run() {
                    if (paly.getStatus() != Play.Status.Success) {
                        status = Play.Status.Failure;

                        if (paly.getStatus() == Play.Status.Failure) {
                            ScenarioLogging.logFailure(log, ParScenario.this, paly.getScenario());
                        } else {
                            ScenarioLogging.logFailure(log, ParScenario.this, paly.getScenario(), paly.getStatus());
                        }
                        
                        future.set(null);
                        cancel();
                    } else if (playsRemain == 0) {
                        status = Play.Status.Success;
                        ScenarioLogging.logSuccess(log, ParScenario.this);
                        future.set(null);
                    }
                }
            });
        }

        @Override
        public void onFailure(Throwable t) {
            boolean justCancelled = cancel();
            
            if (justCancelled) {
                setStatus(Play.Status.Failure);
                ScenarioLogging.logFailure(log, ParScenario.this, t);
                future.setException(t);
            }
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
            
            boolean justCancelled = play.cancel();
            
            if (justCancelled) {
                play.setStatus(Play.Status.Canceled);
                ScenarioLogging.logCancel(log, ParScenario.this);
                super.cancel(false);
            }
            
            return justCancelled;
        };
    }
    
    @Override
    public String toString() {
        return "ParScenario[" + name + "]";
    }
}
