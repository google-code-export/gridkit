package org.gridkit.nimble.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gridkit.nimble.platform.EmptyPlay;
import org.gridkit.nimble.platform.Play;
import org.gridkit.nimble.platform.RemoteAgent;
import org.gridkit.nimble.scenario.ExecScenario;
import org.gridkit.nimble.scenario.ParScenario;
import org.gridkit.nimble.scenario.Scenario;
import org.gridkit.nimble.scenario.ScenarioOps;
import org.gridkit.nimble.util.FutureListener;
import org.gridkit.nimble.util.FutureOps;
import org.gridkit.nimble.util.ValidOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskScenario implements Scenario, FutureListener<Void> {
    private static final Logger log = LoggerFactory.getLogger(TaskScenario.class);
    
    private String name;
    private List<Task> tasks;
    private TaskSLA sla;

    public TaskScenario(String name, Collection<Task> tasks, TaskSLA sla) {
        ValidOps.notEmpty(name, "name");
        ValidOps.notNull(tasks, "tasks");
        ValidOps.notNull(sla, "sla");
        
        this.name = name;
        this.tasks = new ArrayList<Task>(tasks);
        this.sla = sla;
        
        this.sla.shuffle(this.tasks);
    }

    @Override
    public <T> Play<T> play(Context<T> context) {
        ScenarioOps.logStart(log, this);
        
        List<RemoteAgent> agents = sla.getAgents(context.getAgents());
        
        Play<T> result;
        
        if (agents.isEmpty()) {
            TaskOps.logNoAgentsFound(log, this);
            result = new EmptyPlay<T>(
                this, context.getStatsFactory().emptyStats()
            );
        } else if (tasks.isEmpty()) {
            TaskOps.logNoTasksFound(log, this);
            result = new EmptyPlay<T>(
                this, context.getStatsFactory().emptyStats()
            );
        } else if (agents.size() == 1) {
            RemoteAgent execAgent = agents.get(0);
            result = newAgentScenario(execAgent, tasks, TaskOps.getExecName(this, execAgent)).play(context);
        } else {
            List<Scenario> parScenarios = new ArrayList<Scenario>(agents.size());
            
            List<List<Task>> distributedTasks = sla.getDistribution().distribute(tasks, agents.size());
            
            for (int i = 0; i < agents.size(); ++i) {
                List<Task> execTasks = distributedTasks.get(i);
                RemoteAgent execAgent = agents.get(i);
                
                if (!execTasks.isEmpty()) {
                    parScenarios.add(newAgentScenario(execAgent, execTasks, TaskOps.getExecName(this, execAgent)));
                } else {
                    TaskOps.logNoTaskToExecute(log, this, execAgent);
                }
            }
            
            result = (new ParScenario(parScenarios)).play(context);
        }

        FutureOps.addListener(result.getCompletionFuture(), this, context.getExecutor());
        
        return result;
    }
    
    private Scenario newAgentScenario(RemoteAgent agent, List<Task> tasks, String name) {
        TaskExecutable executable = new TaskExecutable(name, tasks, sla);
        Scenario scenario = new ExecScenario(name, executable, agent);
        return scenario;        
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onSuccess(Void result) {
        //TODO fix, failures also go here
        ScenarioOps.logSuccess(log, this);
    }

    @Override
    public void onFailure(Throwable t, FailureEvent event) {
        ScenarioOps.logFailure(log, this, t);
    }

    @Override
    public void onCancel() {
        ScenarioOps.logCancel(log, this);
    }
}
