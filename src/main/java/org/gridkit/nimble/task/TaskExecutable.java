package org.gridkit.nimble.task;

import static org.gridkit.nimble.util.StringOps.F;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.gridkit.nimble.platform.Play;
import org.gridkit.nimble.platform.TimeService;
import org.gridkit.nimble.platform.Play.Status;
import org.gridkit.nimble.scenario.ExecScenario;
import org.gridkit.nimble.scenario.ExecScenario.Context;
import org.gridkit.nimble.scenario.ScenarioOps;
import org.gridkit.nimble.statistics.FlushableStatsReporter;
import org.gridkit.nimble.statistics.StatsReporter;
import org.gridkit.nimble.task.TaskScenario.StatsReporterFactory;
import org.gridkit.util.concurrent.BlockingBarrier;
import org.slf4j.Logger;

//TODO think about start time sync using RemoteAgent.currentTimeMillis()
@SuppressWarnings("serial")
public class TaskExecutable implements ExecScenario.Executable {
    private String name;
    private List<Task> tasks;
    private TaskSLA sla;
    private StatsReporterFactory reporterFactory;
    
    public TaskExecutable(String name, List<Task> tasks, TaskSLA sla, StatsReporterFactory reporterFactory) {
        this.name = name;
        this.tasks = tasks;
        this.sla = sla;
        this.reporterFactory = reporterFactory;
    }

    @Override
    public Play.Status excute(ExecScenario.Context context) {
        AtomicReference<Play.Status> status = new AtomicReference<Play.Status>(Play.Status.Success);
        
        try {
            sla.waitForStart();
        } catch (InterruptedException e) {
            return status.get();
        }
        
        ExecutorService executor = sla.newExecutor(name);
        BlockingBarrier taskBarrier = sla.getTaskBarrier(); 
        
        List<Callable<Void>> taskCallables = new ArrayList<Callable<Void>>();
                
        for (Task task : tasks) {
            taskCallables.add(new TaskCallable(task, status, context, taskBarrier));
        }

        try {
            executor.invokeAll(taskCallables); 
        } catch (InterruptedException ignored) {
            return status.get();
        } finally {
            executor.shutdownNow();
            reporterFactory.flush();
        }
        
        return status.get();
    }
        
    private class TaskCallable implements Callable<Void> {
        private final Task task;
        
        private final AtomicReference<Play.Status> status;
        
        private final ExecScenario.Context context;
        
        private final BlockingBarrier taskBarrier;

        public TaskCallable(Task task, AtomicReference<Play.Status> status, Context context, BlockingBarrier taskBarrier) {
            this.task = task;
            this.status = status;
            this.context = context;
            this.taskBarrier = taskBarrier;
        }

        @Override
        public Void call() throws Exception {
            long startTime = context.getLocalAgent().getTimeService().currentTimeMillis();
            
            FlushableStatsReporter statsReporter = reporterFactory.newTaskReporter();
            
            Task.Context taskContext = new TaskContext(task, context, status, statsReporter);
                        
            try {
                long iteration = 0;
                long duration = 0; 
                
                while (!sla.isFinished(duration, iteration)) {
                    taskBarrier.pass();
                    task.excute(taskContext);
                    
                    iteration += 1;
                    duration = context.getLocalAgent().getTimeService().currentTimeMillis() - startTime;
                }
            } catch (Throwable t) {
                context.getLocalAgent().getLogger(TaskCallable.class.getName()).error(
                    F("Exception during task '%s' execution on agent '%s'", task.toString(), context.getLocalAgent().toString()), t
                );
                status.set(Play.Status.Failure);
            } finally {
                statsReporter.flush();
            }
            
            return null;
        }
    }
    
    private class TaskContext implements Task.Context {
        private final Task task;
        private final ExecScenario.Context context;
        
        private final AtomicReference<Play.Status> status;
        private final StatsReporter reporter;
        
        public TaskContext(Task task, Context context, AtomicReference<Status> status, StatsReporter reporter) {
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
        public TimeService getTimeService() {
            return context.getLocalAgent().getTimeService();
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
        public ConcurrentMap<String, Object> getAttrsMap() {
            return context.getAttrsMap();
        }
    }
    
    @Override
    public String toString() {
        return ScenarioOps.getName("TaskExec", Collections.singleton(name));
    }
}
