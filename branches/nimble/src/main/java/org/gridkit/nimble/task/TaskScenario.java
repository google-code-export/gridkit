package org.gridkit.nimble.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.gridkit.nimble.platform.EmptyPlay;
import org.gridkit.nimble.platform.Play;
import org.gridkit.nimble.platform.RemoteAgent;
import org.gridkit.nimble.scenario.ExecScenario;
import org.gridkit.nimble.scenario.ParScenario;
import org.gridkit.nimble.scenario.Scenario;

public class TaskScenario implements Scenario {
    private static final Logger log = LoggerFactory.getLogger(TaskScenario.class);
    
    private String name;
    private List<Task> tasks;
    private TaskSLA sla;

    public TaskScenario(String name, Collection<Task> tasks, TaskSLA sla) {
        this.name = name;
        this.tasks = new ArrayList<Task>(tasks);
        this.sla = sla;
    }

    @Override
    public <T> Play<T> play(Context<T> context) {
        List<RemoteAgent> agents = sla.getAgents(context.getAgents());
                
        if (agents.isEmpty()) {
            log.info("No agents was found for scenario '{}'", name);
            return new EmptyPlay<T>(
                this, Play.Status.Success, context.getStatsFactory().emptyStats()
            );
        }
        
        List<Scenario> parScenarios = new ArrayList<Scenario>(agents.size());
        
        List<List<Task>> distributedTasks =  sla.getDistribution().distribute(tasks, agents.size());
        
        for (int i = 0; i < agents.size(); ++i) {
            String execName = name + "[" + i + "]";
            List<Task> agentTasks = distributedTasks.get(i);
            
            RemoteAgent execAgent = agents.get(i);
            
            if (!agentTasks.isEmpty()){
                TaskExecutable executable = new TaskExecutable(execName, agentTasks, sla);
                Scenario scenario = new ExecScenario(execName, executable, execAgent);
                
                parScenarios.add(scenario);
            } else {
                log.info("Agent '{}' hasn't tasks to execute in scenario '{}'", execAgent, name);
            }
        }
        
        Scenario parScenario = new ParScenario("Par[" + name + "]", parScenarios);
        
        return parScenario.play(context);
    }

    @Override
    public String getName() {
        return name;
    }
}
