package org.gridkit.lab.orchestration;

public interface PlatformBean {
        
    <T> T ref(String node);
    
    <T> T ref();
    
}
