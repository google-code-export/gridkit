package org.gridkit.lab.orchestration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.gridkit.lab.orchestration.script.Script;
import org.gridkit.lab.orchestration.script.ScriptBuilder;
import org.gridkit.lab.orchestration.util.ClassOps;

public class ScenarioBuilder implements Platform.ScriptConstructor {        
    private Platform platform;
    private ScriptBuilder builder = new ScriptBuilder();
    private LocalOps localOps = new LocalOps();
    private Scope scope = Scopes.any();
    private Map<String, String> captions = new HashMap<String, String>();
    private int sectionNum = 0;
    private int nextPointNum = 0;
    
    protected ScenarioBuilder(Platform platform) {
        this.platform = platform;
        fromStart();
    }

    public SectionOptions from(String name) {
        builder.from(name);
        builder.par();
        scope = Scopes.any();
        sectionNum += 1;
        return new SectionOptions();
    }
    
    public SectionOptions fromStart() {
        return from(ScriptBuilder.START);
    }
    
    public SectionOptions join(String name) {
        builder.join(name);
        sectionNum += 1;
        // scope and par mode are not changed
        return new SectionOptions();
    }
    
    public void joinFinish() {
        join(ScriptBuilder.FINISH);
    }

    public void sync() {
        String location = ClassOps.toString(ClassOps.location(1));
        String name = "sync-" + (nextPointNum++) + " at " + location;
        builder.join(name);
        captions.put(name, "sync() at " + location);
    }
    
    private void sleep(long timeout, TimeUnit unit, StackTraceElement location) {
        SourceRef ref = new SourceRef("sleep(" + timeout + " " + toString(unit) + ")", location);
        
        int num = (nextPointNum++);
        String before = "before-" +  num + ref.toString();
        String after = "after-" + num + ref.toString();
        
        builder.join(before);
        builder.action(new Sleep(ref, timeout, unit));
        builder.join(after);
        
        captions.put(before, null);
        captions.put(after, null);
    }
    
    public void sleep(long timeout, TimeUnit unit) {
        sleep(timeout, unit, ClassOps.location(1));
    }
    
    public void sleep(long millis) {
        sleep(millis, TimeUnit.MILLISECONDS, ClassOps.location(1));
    }
    
    public RemoteOps at(Scope scope) {
        return new RemoteOps(Scopes.and(this.scope, scope));
    }
    
    public RemoteOps at(String pattern) {
        return at(Scopes.pattern(pattern)); 
    }
    
    public LocalOps local() {
        return localOps;
    }
    
    public ScriptBuilder getScriptBuilder() {
        return builder;
    }
    
    public Scope getScope() {
        return scope;
    }
    
    public Scenario build() {
        joinFinish();
        Script script = builder.getScript();
        platform.clearScriptConstructor();
        builder = null;
        return new Scenario(script, platform, captions);
    }
    
    private class SectionObject {
        private int num = ScenarioBuilder.this.sectionNum;
        
        protected void validate() {
            if (ScenarioBuilder.this.sectionNum != num) {
                throw new IllegalStateException("section object is not vailid");
            }
        }
    }
    
    public class RemoteOps extends SectionObject {
        private Scope scope;
        
        public RemoteOps(Scope scope) {
            this.scope = Scopes.and(ScenarioBuilder.this.scope, scope);
        }

        public <T> T deploy(T prototype) {
            validate();
            RemoteBean.Deploy bean = RemoteBean.newDeploy(prototype, scope, ClassOps.location(1));
            builder.action(bean);
            return bean.<T>getProxy(platform);
        }
        
        public <T> T bean(T bean) {
            validate();
            return platform.at(scope).bean(bean);
        }
    }
    
    public class LocalOps {
        public <T> T deploy(T prototype) {
            return null;
        }
    }
    
    public class SectionOptions extends SectionObject {
        public SectionOptions par() {
            validate();
            builder.par();
            return this;
        }
        
        public SectionOptions seq() {
            validate();
            builder.seq();
            return this;
        }
        
        public SectionOptions scope(Scope scope) {
            validate();
            ScenarioBuilder.this.scope = scope;
            return this;
        }
        
        public SectionOptions scope(String pattern) {
            return scope(Scopes.pattern(pattern));
        }
    }
    
    private static String toString(TimeUnit unit) {
        if (TimeUnit.NANOSECONDS == unit) {
            return "ns";
        } else if (TimeUnit.MICROSECONDS == unit) {
            return "us";
        } else if (TimeUnit.MILLISECONDS == unit) {
            return "ms";
        } else if (TimeUnit.SECONDS == unit) {
            return "s";
        } else if (TimeUnit.MINUTES == unit) {
            return "m";
        } else if (TimeUnit.HOURS == unit) {
            return "h";
        } else if (TimeUnit.DAYS == unit) {
            return "d";
        } else {
            throw new IllegalArgumentException();
        }
    }
}
