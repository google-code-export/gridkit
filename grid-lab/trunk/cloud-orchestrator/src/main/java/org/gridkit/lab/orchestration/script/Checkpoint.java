package org.gridkit.lab.orchestration.script;

import java.io.Serializable;

import org.gridkit.util.concurrent.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Checkpoint implements ScriptAction, Serializable {
    private static final long serialVersionUID = 7844703208347065282L;
    
    private static final Logger log = LoggerFactory.getLogger(Checkpoint.class);
    
    public interface Listener {
        void checkpoint(String name);
    }
    
    private String name;
    private Listener listener = EmptyListener.INSTANCE;
    
    @Override
    public void execute(Box<Void> box) {
        listener.checkpoint(name);
        box.setData(null);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setListener(Listener listener) {
        this.listener = listener;
    }
    
    public Listener getListener() {
        return listener;
    }
    
    public static class LogListener implements Listener, Serializable {
        public static final LogListener INSTANCE = new LogListener();
        
        private static final long serialVersionUID = -7131464241474343197L;

        @Override
        public void checkpoint(String name) {
            log.info("Checkpoint '" + name + "' reached");
        }
    }
    
    public static class EmptyListener implements Listener, Serializable {
        public static final EmptyListener INSTANCE = new EmptyListener();
        
        private static final long serialVersionUID = -3594145224139465294L;

        @Override
        public void checkpoint(String name) {}
    }
}
