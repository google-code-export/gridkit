package org.gridkit.nimble.task;

import static org.gridkit.nimble.util.StringOps.F;

import java.util.ArrayList;
import java.util.Collections;
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
import org.gridkit.nimble.scenario.ScenarioOps;
import org.gridkit.nimble.statistics.StatsMonoid;
import org.gridkit.nimble.statistics.StatsProducer;
import org.gridkit.nimble.statistics.StatsReporter;
import org.gridkit.util.concurrent.BlockingBarrier;

//TODO think about start time sync using RemoteAgent.currentTimeMillis()
@SuppressWarnings("serial")
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
        
        ExecutorService executor = sla.newExecutor(name);
        BlockingBarrier taskBarrier = sla.getTaskBarrier(); 
        
        List<Callable<Void>> taskCallables = new ArrayList<Callable<Void>>();
                
        for (Task task : tasks) {
            taskCallables.add(new TaskCallable<T>(task, status, stats, context, taskBarrier));
        }

        try {
            executor.invokeAll(taskCallables); 
        } catch (InterruptedException ignored) {
            return getResult(status, stats);
        } finally {
            executor.shutdownNow();
        }
        
        return getResult(status, stats);
    }
    
    private <T> Result<T> getResult(AtomicReference<Play.Status> status, AtomicReference<T> stats) {
        return new Result<T>(sla.getStatus(status.get()), stats.get());
    }
    
    private class TaskCallable<T> implements Callable<Void> {
        private final Task task;
        
        private final AtomicReference<Play.Status> status;
        private final AtomicReference<T> stats;
        
        private final ExecScenario.Context<T> context;
        
        private final BlockingBarrier taskBarrier;

        public TaskCallable(Task task, AtomicReference<Play.Status> status, AtomicReference<T> stats, Context<T> context, BlockingBarrier taskBarrier) {
            this.task = task;
            this.status = status;
            this.stats = stats;
            this.context = context;
            this.taskBarrier = taskBarrier;
        }

        @Override
        public Void call() throws Exception {
            long startTime = context.getLocalAgent().currentTimeMillis();
            
            StatsMonoid<T> statsFactory = context.getStatsFactory();
            StatsProducer<T> statsProducer = statsFactory.newStatsProducer();
            
            Task.Context taskContext = new TaskContext<T>(task, context, status, statsProducer);
                        
            try {
                long iteration = 0;
                long duration = 0; 
                
                while (!sla.isFinished(duration, iteration)) {
                    taskBarrier.pass();
                    task.excute(taskContext);
                    
                    iteration += 1;
                    duration = context.getLocalAgent().currentTimeMillis() - startTime;
                }
            } catch (Throwable t) {
                context.getLocalAgent().getLogger(TaskCallable.class.getName()).error(
                    F("Exception during task '%s' execution on agent '%s'", task.toString(), context.getLocalAgent().toString()), t
                );
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
        private final Task task;
        private final ExecScenario.Context<T> context;
        
        private final AtomicReference<Play.Status> status;
        private final StatsReporter reporter;
        
        public TaskContext(Task task, Context<T> context, AtomicReference<Status> status, StatsReporter reporter) {
            this.task = task;
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
        public long currentTimeNanos() {
            return context.getLocalAgent().currentTimeNanos();
        }
        
        @Override
        public Logger getLogger() {
            return context.getLocalAgent().getLogger(task.getName());
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
    
    @Override
    public String toString() {
        return ScenarioOps.getName("TaskExec", Collections.singleton(name));
    }
}
