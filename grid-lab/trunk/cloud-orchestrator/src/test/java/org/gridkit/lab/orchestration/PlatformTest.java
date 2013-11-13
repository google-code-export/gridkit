package org.gridkit.lab.orchestration;

import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViNodeSet;
import org.gridkit.vicluster.ViProps;
import org.junit.Test;

public class PlatformTest {
    @Test
    public void test() {
        ViNodeSet cloud = CloudFactory.createCloud();
        ViProps.at(cloud.node("**")).setIsolateType();
        Platform platform = new Platform(cloud);
        
        // --- Start scenario
        
        HookBuilder hb = platform.onStart("**");
            Printer printer = hb.deploy(new Printer.Impl());
            printer.out("all nodes on start");
        hb.build();
        
        hb = platform.onStart("ttt");
            printer.err("ttt node onstart");
        hb.build();
        
        // ---
        
        cloud.nodes("ttt", "qqq").touch();
        
        ScenarioBuilder sb = platform.newScenario();
            printer.out("scenario 1");
            sb.sync();
            printer.out("scenario 2");
        sb.build().play();
        
        sb = platform.newScenario();
            printer.exception();
        try {
            sb.build().play();
        } catch (RuntimeException e) {}
        
        
        printer.out("out of scenario");
        
        printer.function().run();
        
        printer.out("end of out of scenario");
        
        /*
        
        // --- Group calls
        
        Printer groupPrinter = platform.group(printer, Scopes.any());
        
        groupPrinter.err("after start");
        
        // --- Direct calls
        
        Printer directPrinter = platform.direct(printer, "ttt");
        
        System.err.println(directPrinter.i("direct call"));
        
        Runnable function = directPrinter.function();
        
        function.run();
        
        // --- Exceptions
        
        try {
            groupPrinter.exception();
        } catch (IllegalStateException e) {
            System.err.println("catched group " + e);
        }
        
        try {
            directPrinter.exception();
        } catch (IllegalStateException e) {
            System.err.println("catched direct " + e);
        }
        
        */
    }
}
