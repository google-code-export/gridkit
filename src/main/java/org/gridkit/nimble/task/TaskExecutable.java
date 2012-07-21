package org.gridkit.nimble.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;

import org.gridkit.nimble.platform.Play;
import org.gridkit.nimble.platform.Play.Status;
import org.gridkit.nimble.scenario.ExecScenario;
import org.gridkit.nimble.scenario.ExecScenario.Context;
import org.gridkit.nimble.scenario.ExecScenario.Result;
import org.gridkit.nimble.statistics.StatsFactory;
import org.gridkit.nimble.statistics.StatsProducer;
import org.gridkit.nimble.statistics.StatsReporter;

//TODO think about start time sync using RemoteAgent.currentTimeMillis()
public class TaskExecutable implements ExecScenario.Executable {
    private String name;
    private List<Task> tasks;
    private TaskSLA sla;
    
    public TaskExecutable(String name, List<Task> tasks, TaskSLA sla) {
        this.name = name;
        this.tasks = tasks;
        this.sla = sla;
    }

    @Override
    public <T> Result<T> excute(ExecScenario.Context<T> context) {
        AtomicReference<Play.Status> status = new AtomicReference<Play.Status>(Play.Status.Success);
        AtomicReference<T> stats = new AtomicReference<T>(context.getStatsFactory().emptyStats());
        
        try {
            sla.waitForStart();
        } catch (InterruptedException e) {
            return getResult(status, stats);
        }
        
        ExecutorService executor = sla.newExecutor();
        
        List<Callable<Void>> taskCallables = new ArrayList<Callable<Void>>();
        
        for (Task task : tasks) {
            taskCallables.add(new TaskCallable<T>(task, status, stats, context));
        }

        try {
            executor.invokeAll(taskCallables);
        } catch (InterruptedException e) {
            return getResult(status, stats);
        } finally {
            executor.shutdownNow();
        }
        
        return null;
    }
    
    private <T> Result<T> getResult(AtomicReference<Play.Status> status, AtomicReference<T> stats) {
        return new Result<T>(sla.getStatus(status.get()), stats.get());
    }
    
    private class TaskCallable<T> implements Callable<Void> {
        private final Task task;
        
        private final AtomicReference<Play.Status> status;
        private final AtomicReference<T> stats;
        
        private final ExecScenario.Context<T> context;
        
        public TaskCallable(Task task, AtomicReference<Play.Status> status, AtomicReference<T> stats, Context<T> context) {
            this.task = task;
            this.status = status;
            this.stats = stats;
            this.context = context;
        }

        @Override
        public Void call() throws Exception {
            long startTime = context.getLocalAgent().currentTimeMillis();
            
            StatsFactory<T> statsFactory = context.getStatsFactory();
            StatsProducer<T> statsProducer = statsFactory.newStatsProducer();
            
            Task.Context taskContext = new TaskContext<T>(context, status, statsProducer);
            
            try {
                long iteration = 0;
                long duration = context.getLocalAgent().currentTimeMillis() - startTime;
                
                while (sla.isFinished(duration, iteration)) {
                    iteration += 1;
                    task.excute(taskContext);
                }
            } catch (Throwable t) {
                statsProducer.report(t.toString(), context.getLocalAgent().currentTimeMillis());
                status.set(Play.Status.Failure);
            } finally {
                synchronized (stats) {
                    stats.set(statsFactory.combine(stats.get(), statsProducer.produce()));
                }
            }
            
            return null;
        }
    }
    
    private class TaskContext<T> implements Task.Context {
        private final ExecScenario.Context<T> context;
        
        private final AtomicReference<Play.Status> status;
        private final StatsReporter reporter;
        
        public TaskContext(Context<T> context, AtomicReference<Status> status, StatsReporter reporter) {
            this.context = context;
            this.status = status;
            this.reporter = reporter;
        }

        @Override
        public StatsReporter getStatReporter() {
            return reporter;
        }

        @Override
        public long currentTimeMillis() {
            return context.getLocalAgent().currentTimeMillis();
        }

        @Override
        public Logger getLogger() {
            return context.getLocalAgent().getLogger(name);
        }

        @Override
        public void setFailure() {
            status.set(Play.Status.Failure);
        }

        @Override
        public ConcurrentMap<String, Object> getAttributesMap() {
            return context.getAttributesMap();
        }
    }
}
