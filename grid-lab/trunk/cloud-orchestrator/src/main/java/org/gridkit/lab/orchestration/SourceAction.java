package org.gridkit.lab.orchestration;

public interface SourceAction {
    StackTraceElement getLocation();
    
    String getSource();
}
