package com.griddynamics.java.benchmarks.engine;

import com.griddynamics.java.benchmarks.model.Group;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * User: akondratyev
 * Date: Dec 16, 2010
 * Time: 7:21:50 PM
 */
public class Experiment {

    private static Logger logger = LogManager.getLogger(Experiment.class);

    public static void main(String[] args) {
        logger.info("start program.. press enter");

        try {
            System.in.read();
        } catch (IOException e) {
            logger.error("waiting user enter", e);
        }

        Properties props = new Properties();
        FileReader propsReader;
        try {
            propsReader = new FileReader("gcbenchmark.properties");
            props.load(propsReader);
            propsReader.close();
        } catch (FileNotFoundException e) {
            logger.error("Couldn't find gcbecnmark.property " + e);
            System.exit(1);
        } catch (IOException e) {
            logger.error("Couldn't load params from gcbecnmark.properties " + e);
            System.exit(1);
        }

        TickCounter tickCounter = new TickCounter();

        logger.info("creating groups....");
        ConcurrentLinkedQueue<Group> groupQueue = new ConcurrentLinkedQueue<Group>();
        fillGroupQueue(groupQueue, props, tickCounter);
        logger.info("groups: \\n" + groupQueue);

        if (!props.containsKey("threadsCount"))
            throw new NullPointerException("there's no threadsCount in property file");

        ExecutorService threads = Executors.newFixedThreadPool(Integer.parseInt((String) props.get("threadsCount")));
        for (int i = 0; i < Integer.parseInt((String) props.get("threadsCount")); i++) {
            threads.execute(new EntityCreator(groupQueue, tickCounter));
        }
        threads.shutdown();

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("ticks = " + tickCounter.getTicks());
        System.out.println("stop program after 1 minute");
        System.exit(0);
    }

    public static void fillGroupQueue(Queue<Group> queue, Properties props, TickCounter tickCounter) {
        if (!props.containsKey("groupCount") || !props.containsKey("groupObjectLifeTime") ||
                !props.containsKey("groupObjectSize") || !props.containsKey("groupObjectsCount")) {
            throw new NullPointerException("groupObjectsCount/groupObjectLifeTime/groupObjectSize/groupObjectsCount" +
                    " doesn't exist in property file");
        }
        for (int i = 0; i < Integer.parseInt((String) props.get("groupCount")); i++) {

            //TODO: props set difficult
            Group gr = new Group.Builder()
                    .setObjectLifeTime(Integer.parseInt(((String) props.get("groupObjectLifeTime")).split(":")[i]))
                    .setMaxObjectsCount(Integer.parseInt(((String) props.get("groupObjectsCount")).split(":")[i]))
                    .setObjectSize(Integer.parseInt(((String) props.get("groupObjectSize")).split(":")[i]))
                    .setTickCounter(tickCounter)
                    .build();
            queue.add(gr);
        }
    }


}
