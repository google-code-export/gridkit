package org.gridkit.lab.orchestration;

import org.gridkit.lab.orchestration.script.CycleDetectedException;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViNodeSet;
import org.gridkit.vicluster.ViProps;

public class CycleCheck {
    public static void main(String[] args) {
        ViNodeSet cloud = CloudFactory.createCloud();
        ViProps.at(cloud.node("**")).setIsolateType();
        Platform platform = new Platform(cloud);

        try {
            ScenarioBuilder sb = platform.newScenario();
            
            sb.from("A");
                Printer printer = sb.node("**").deploy(new Printer.Impl());
            sb.join("B");
            
            sb.from("B");
                printer.out("out");
            sb.join("A");
            
            sb.build();
        } catch (CycleDetectedException e) {
            
        }
    }
}
