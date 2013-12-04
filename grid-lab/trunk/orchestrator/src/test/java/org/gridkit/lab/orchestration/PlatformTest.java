package org.gridkit.lab.orchestration;

import org.junit.Test;

public class PlatformTest {
    @Test
    public void test() {
        Platform platform = new Platform();
        
        // --- Hook node initialization
        // --- Just sequence of methods executed on startup in single thread
        // --- No global synchronization or parallel execution
        
        HookBuilder hb = platform.onStart();
            Printer printer = hb.deploy(new Printer.Impl());
            printer.out("all nodes on start");
        hb.build();
        
        hb = platform.onStart("ttt");
            printer.err("ttt node onstart");
        hb.build();

        platform.cloud().nodes("ttt", "qqq").touch();
        
        System.err.println("\n------\n");
        
        // --- Example of working with driver out of scenario
        // ---
        
        printer.out("out of scenario");
        
        printer.function().run();
                
        Printer printerTTT = platform.at("ttt").deploy(new Printer.Impl());
        
        printerTTT.out("only on ttt");

        System.err.println("\n------\n");
        
        // --- Example of scenario construction and execution
        // ---
        
        ScenarioBuilder sb = platform.newScenario();
            
            sb.from("A").par();
        
                printer.sleep(100);
            
                printer.out("scenario");
                
                sb.sync();
                
                printer.out("before sleep");
                
                sb.sleep(100);
                
                sb.at("qqq").bean(printer).out("qqq after sleep");
                
           sb.join("B").seq().scope("ttt");
           
               Printer localPrinter = sb.local(new Printer.Impl());
           
               localPrinter.out("local before seq");
               printer.out("1");
               printer.out("2");
               printer.out("3");
               localPrinter.function().run();
               
               sb.at("qqq").bean(printer).out("no nodes");
           
           sb.join("C");
            
        sb.build().play();
        
        //sb = platform.newScenario();
        //    printer.exception();
        //try { sb.build().play(); } catch (RuntimeException e) {}
    }
}
