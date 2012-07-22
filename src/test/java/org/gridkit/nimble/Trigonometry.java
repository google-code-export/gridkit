package org.gridkit.nimble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.platform.Director;
import org.gridkit.nimble.platform.Play;
import org.gridkit.nimble.platform.RemoteAgent;
import org.gridkit.nimble.platform.local.ThreadPoolAgent;
import org.gridkit.nimble.scenario.ParScenario;
import org.gridkit.nimble.scenario.Scenario;
import org.gridkit.nimble.scenario.SeqScenario;
import org.gridkit.nimble.statistics.StatsOps;
import org.gridkit.nimble.statistics.ThroughputSummary;
import org.gridkit.nimble.statistics.simple.SimpleStats;
import org.gridkit.nimble.statistics.simple.SimpleStatsFactory;
import org.gridkit.nimble.statistics.simple.SimpleStatsReporter;
import org.gridkit.nimble.task.Task;
import org.gridkit.nimble.task.TaskSLA;
import org.gridkit.nimble.task.TaskScenario;
import org.junit.Ignore;

import com.google.common.base.Function;

@Ignore
public class Trigonometry {    
    private static final String SIN = "sin";
    private static final String COS = "cos";
    private static final String TAN = "tan";
    
    private static final long WARMUP_NUMBERS = 5;
    private static final long WARMUP_ITERATIONS = 25000;
    private static final long WARMUP_DURATION = 3; // seconds
    
    private static final long NUMBERS = 10;
    private static final long ITERATIONS = 100000;
    private static final long DURATION = 3; // seconds
    
    public static void main(String[] args) throws Exception {
        ExecutorService agentExecutor = Executors.newCachedThreadPool();
        ExecutorService directorExecutor = Executors.newCachedThreadPool();
        
        RemoteAgent sinAgent = new ThreadPoolAgent(agentExecutor, Collections.singleton(SIN));
        RemoteAgent cosAgent = new ThreadPoolAgent(agentExecutor, Collections.singleton(COS));
        
        Director<SimpleStats> director = new Director<SimpleStats>(
            Arrays.asList(sinAgent, cosAgent), new SimpleStatsFactory(), directorExecutor
        );

        Play<SimpleStats> play;
        
        try {
            play = director.play(getScenario(WARMUP_NUMBERS, WARMUP_ITERATIONS, WARMUP_DURATION));
            play.getCompletionFuture().get();
            
            play = director.play(getScenario(NUMBERS, ITERATIONS, DURATION));
            play.getCompletionFuture().get();
        } finally {
            director.shutdown(false);
        }

        for (String func : Arrays.asList(SIN, COS, TAN)) {
            StatisticalSummary latencyStats = play.getStats().getLatency(calcStats(func), TimeUnit.MICROSECONDS);
            ThroughputSummary throughput = play.getStats().getThroughput(calcStats(func));

            System.err.println("----- Latency for " + func);
            System.err.println(StatsOps.latencyToString(latencyStats, TimeUnit.MICROSECONDS));
            System.err.println("----- Throughput of " + func);
            System.err.println(StatsOps.throughputToString(throughput, TimeUnit.MILLISECONDS));
        }
    }
    
    private static Scenario getScenario(long numbers, long iterations, long duration) {
        Task sinInitTask = new InitTask("SinInitTask", SIN, new Sin());
        Task cosInitTask = new InitTask("CosInitTask", COS, new Cos());
        Task tanInitTask = new InitTask("TanInitTask", TAN, new Tan());
        
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
        sinSLA.setIterationsCount(iterations);
        
        cosSLA = cosSLA.clone();
        cosSLA.setIterationsCount(iterations);
        
        tanSLA = tanSLA.clone();
        tanSLA.setFinishDelay(duration, TimeUnit.SECONDS);
        tanSLA.setIterationsCount(null);
        
        List<Task> sinTasks = new ArrayList<Task>();
        List<Task> cosTasks = new ArrayList<Task>();
        List<Task> tanTasks = new ArrayList<Task>();
        
        for (long i = 1; i <= numbers; ++i) {
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
            return name;
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
            
            String initStats = initStats(funcName);
            String calsStats = calcStats(funcName);
            
            reporter.start(initStats);
            @SuppressWarnings("unchecked")
            Function<Double, Double> func = (Function<Double, Double>)context.getAttributesMap().get(funcName);
            reporter.finish(initStats);
            
            reporter.start(calsStats);
            func.apply(value);
            reporter.finish(calsStats);
        }

        @Override
        public String getName() {
            return name;
        }
    }
    
    public static String initStats(String name) {
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
