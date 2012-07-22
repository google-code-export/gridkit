package org.gridkit.nimble;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gridkit.nimble.platform.Director;
import org.gridkit.nimble.platform.Play;
import org.gridkit.nimble.platform.RemoteAgent;
import org.gridkit.nimble.platform.local.ThreadPoolAgent;
import org.gridkit.nimble.scenario.ExecScenario;
import org.gridkit.nimble.scenario.ExecScenario.Context;
import org.gridkit.nimble.scenario.ExecScenario.Executable;
import org.gridkit.nimble.scenario.ExecScenario.Result;
import org.gridkit.nimble.scenario.ParScenario;
import org.gridkit.nimble.scenario.Scenario;
import org.gridkit.nimble.scenario.SeqScenario;
import org.gridkit.nimble.statistics.simple.SimpleStats;
import org.gridkit.nimble.statistics.simple.SimpleStatsFactory;
import org.slf4j.Logger;

public class Sleep {
    public static void main(String[] args) throws Exception {
        ExecutorService agentExecutor = Executors.newCachedThreadPool();
        ExecutorService directorExecutor = Executors.newCachedThreadPool();

        RemoteAgent agent = new ThreadPoolAgent(agentExecutor);
        
        Director<SimpleStats> director = new Director<SimpleStats>(
            Collections.singletonList(agent),
            new SimpleStatsFactory(),
            directorExecutor
        );
        
        Scenario s1 = new ExecScenario(new SimpleExecutable("A"), agent);
        Scenario s2 = new ExecScenario(new SimpleExecutable("B"), agent);
        Scenario s3 = new ExecScenario(new SimpleExecutable("C"), agent);
        
        Scenario s4 = new ExecScenario(new SimpleExecutable("D"), agent);
        Scenario s5 = new ExecScenario(new SimpleExecutable("E"), agent);
        Scenario s6 = new ExecScenario(new SimpleExecutable("F"), agent);
        
        Scenario seq1 = new SeqScenario("SEQ1", Arrays.asList(s1, s2, s3));
        Scenario seq2 = new SeqScenario("SEQ2", Arrays.asList(s4, s5, s6));

        Scenario par = new ParScenario("PAR", Arrays.asList(seq1, seq2));
        
        Play<SimpleStats> play = director.play(par);
        
        Thread.sleep(1250);
        
        System.out.println(play.getStatus());
        
        play.getCompletionFuture().cancel(true);
        
        try {
            play.getCompletionFuture().get();
        } finally {
            Thread.sleep(100);
            System.out.println(play.getStatus());
            
            agentExecutor.shutdown();
            directorExecutor.shutdown();
        }
    }
    
    public static class SimpleExecutable implements Executable {
        private final String str;

        public SimpleExecutable(String str) {
            this.str = str;
        }

        @Override
        public <T> Result<T> excute(Context<T> context) throws Exception {
            Logger log = context.getLocalAgent().getLogger(str);
            
            log.info(str + " - before sleep");
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.info(str + " was interrupted");
            }
            
            log.info(str + " - after sleep");
            
            return new Result<T>(Play.Status.Success, context.getStatsFactory().emptyStats());
        }
        
        @Override
        public String toString() {
            return "SimpleExecutable[" + str + "]";
        }
    }
}
