package org.gridkit.lab.orchestration;

public interface PlatformBean {
    
    <T> T getRef(String node);
    
    <T> T getRef();
    
}
