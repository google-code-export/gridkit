package org.gridkit.nimble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gridkit.nimble.platform.Director;
import org.gridkit.nimble.platform.Play;
import org.gridkit.nimble.platform.RemoteAgent;
import org.gridkit.nimble.platform.local.ThreadPoolAgent;
import org.gridkit.nimble.scenario.ParScenario;
import org.gridkit.nimble.scenario.Scenario;
import org.gridkit.nimble.scenario.SeqScenario;
import org.gridkit.nimble.statistics.simple.SimpleStats;
import org.gridkit.nimble.statistics.simple.SimpleStatsFactory;
import org.gridkit.nimble.statistics.simple.SimpleStatsReporter;
import org.gridkit.nimble.task.Task;
import org.gridkit.nimble.task.TaskSLA;
import org.gridkit.nimble.task.TaskScenario;

import com.google.common.base.Function;

public class Trigonometry {
    private static final String SIN = "sin";
    private static final String COS = "cos";
    private static final String TAN = "tan";
    
    private static final long NUMBERS = 5;
    private static final long ITERATIONS = 5;
    private static final long DURATION = 5000;
    
    public static void main(String[] args) throws Exception {
        ExecutorService agentExecutor = Executors.newCachedThreadPool();
        ExecutorService directorExecutor = Executors.newCachedThreadPool();
        
        RemoteAgent sinAgent = new ThreadPoolAgent(agentExecutor, Collections.singleton(SIN));
        RemoteAgent cosAgent = new ThreadPoolAgent(agentExecutor, Collections.singleton(COS));
        
        Director<SimpleStats> director = new Director<SimpleStats>(
            Arrays.asList(sinAgent, cosAgent), new SimpleStatsFactory(), directorExecutor
        );

        Play<SimpleStats> play = director.play(getScenario());
        
        try {
            play.getCompletionFuture().get();
        } finally {
            System.err.println("--------------------");
            director.shutdown(false);
        }
    }
    
    private static Scenario getScenario() {
        Task sinInitTask = new InitTask("SinInitTask", SIN, new Sin());
        Task cosInitTask = new InitTask("CosInitTask", COS, new Cos());
        Task tanInitTask = new InitTask("TanInitTask", TAN, new Cos());
        
        TaskSLA sinSLA = new TaskSLA();
        sinSLA.setLabels(Collections.singleton(SIN));

        TaskSLA cosSLA = new TaskSLA();
        cosSLA.setLabels(Collections.singleton(COS));
        
        TaskSLA tanSLA = new TaskSLA();
        tanSLA.setLabels(new HashSet<String>(Arrays.asList(SIN, COS)));
        
        Scenario sinInitScen = new TaskScenario(
            sinInitTask.getName(), Collections.singleton(sinInitTask), sinSLA
        );
        
        Scenario cosInitScen = new TaskScenario(
            cosInitTask.getName(), Collections.singleton(cosInitTask), cosSLA
        );
        
        Scenario tanInitScen = new TaskScenario(
            tanInitTask.getName(), Collections.singleton(tanInitTask), tanSLA
        );
        
        sinSLA = sinSLA.clone();
        sinSLA.setIterationsCount(ITERATIONS);
        
        cosSLA = cosSLA.clone();
        cosSLA.setIterationsCount(ITERATIONS);
        
        tanSLA = tanSLA.clone();
        tanSLA.setFinishDelay(DURATION);
        
        List<Task> sinTasks = new ArrayList<Task>();
        List<Task> cosTasks = new ArrayList<Task>();
        List<Task> tanTasks = new ArrayList<Task>();
        
        for (long i = 1; i <= NUMBERS; ++i) {
            sinTasks.add(new CalcTask("SinCalcTask#"+i, SIN, i));
            cosTasks.add(new CalcTask("CosCalcTask#"+i, COS, i));
            tanTasks.add(new CalcTask("TanCalcTask#"+i, TAN, i));
        }
        
        Scenario sinCalcScen = new TaskScenario(
            "sin cals scen", sinTasks, sinSLA
        );
            
        Scenario cosCalcScen = new TaskScenario(
            "cos cals scen", cosTasks, cosSLA
        );
            
        Scenario tanCalsScen = new TaskScenario(
            "tan cals scen", tanTasks, tanSLA
        );

        Scenario init = new ParScenario(Arrays.asList(sinInitScen, cosInitScen, tanInitScen));
        
        Scenario first = new ParScenario(Arrays.asList(sinCalcScen, cosCalcScen));
        
        return new SeqScenario(Arrays.asList(init, first, tanCalsScen));
    }    
    
    public static class InitTask implements Task {
        private final String name;
        private final String funcName;
        private final Function<Double, Double> func;

        public InitTask(String name, String funcName, Function<Double, Double> func) {
            this.name = name;
            this.funcName = funcName;
            this.func = func;
        }

        @Override
        public void excute(Context context) throws Exception {
            Thread.sleep(250);
            context.getLogger().info("log " + name);
            context.getAttributesMap().put(funcName, func);
        }

        @Override
        public String getName() {
            return "init of " + name;
        }
    }
    
    public static class CalcTask implements Task {
        private final String name;
        private final String funcName;
        private final double value;
        
        public CalcTask(String name, String funcName, double value) {
            this.name = name;
            this.funcName = funcName;
            this.value = value;
        }

        @Override
        public void excute(Context context) throws Exception {
            SimpleStatsReporter reporter = new SimpleStatsReporter(context.getStatReporter(), context);
            
            String getStats = getStats(funcName);
            String calsStats = calcStats(funcName);
            
            reporter.start(getStats);
            @SuppressWarnings("unchecked")
            Function<Double, Double> func = (Function<Double, Double>)context.getAttributesMap().get(funcName);
            reporter.finish(getStats);
            
            reporter.start(calsStats);
            func.apply(value);
            reporter.finish(calsStats);
        }

        @Override
        public String getName() {
            return "calculation of " + name;
        }
    }
    
    public static String getStats(String name) {
        return "get_" + name;
    }
    
    public static String calcStats(String name) {
        return "calc_" + name;
    }
    
    public static class Sin implements Function<Double, Double> {
        @Override
        public Double apply(Double arg) {
            return Math.sin(arg);
        }
    }
    
    public static class Cos implements Function<Double, Double> {
        @Override
        public Double apply(Double arg) {
            return Math.cos(arg);
        }
    }
    
    public static class Tan implements Function<Double, Double> {
        @Override
        public Double apply(Double arg) {
            return Math.tan(arg);
        }
    }
}
