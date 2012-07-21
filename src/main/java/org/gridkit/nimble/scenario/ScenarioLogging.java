package org.gridkit.nimble.scenario;

import org.slf4j.Logger;

import org.gridkit.nimble.platform.Play;

import static org.gridkit.nimble.util.StringOps.F;

public class ScenarioLogging {
    public static void logStart(Logger log, Scenario scenario) {
        log.info("Starting execution of Scenario '{}'", scenario.getName());
    }

    public static void logSuccess(Logger log, Scenario scenario) {
        log.info("Scenario '{}' finished succesfully", scenario.getName());
    }
    
    public static void logCancel(Logger log, Scenario scenario) {
        log.info("Scenario '{}' was canceled", scenario.getName());
    }
    
    public static void logFailure(Logger log, Scenario scenario, Throwable t) {
        if (log.isWarnEnabled()) {
            if (t != null) {
                log.warn(F("Scenario '%s' was failed due to exception", scenario.getName()), t);
            } else {
                log.warn(F("Scenario '%s' was failed. Exception is missed", scenario.getName()), t);
            }
        }
    }
    
    public static void logFailure(Logger log, Scenario scenario, String cause) {
        log.warn("Scenario '{}' was failed due to fail of '{}'", scenario.getName(), cause);
    }
    
    public static void logFailure(Logger log, Scenario scenario, String cause, Play.Status status) {
        if (log.isErrorEnabled()) {
            log.error(F(
                "Scenario '%s' got incorrect status '%s' from scenario '{}' and failed",
                scenario.getName(), status, cause
            ));
        }
    }
    
    public static void logFailure(Logger log, Scenario master, Scenario worker) {
        logFailure(log, master, F("scenarion '%s'", worker.getName()));
    }
    
    public static void logFailure(Logger log, Scenario master, Scenario worker, Play.Status status) {
        logFailure(log, master, F("scenarion '%s'", worker.getName()), status);
    }
}
