package org.gridkit.lab.orchestration;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.gridkit.vicluster.GlobOps;

public class Scopes {
    public static PatternScope any() {
        return pattern("**");
    }
    
    public static PatternScope pattern(String pattern) {
        return new PatternScope(pattern);
    }
    
    public static class PatternScope implements Scope, Serializable {
        private static final long serialVersionUID = -2153009740049280455L;
        
        private final Pattern pattern;
        
        public PatternScope(String pattern) {
            this.pattern = GlobOps.translate(pattern, ".");
        }

        @Override
        public boolean contains(String node) {
            return pattern.matcher(node).matches();
        }
    }
}
