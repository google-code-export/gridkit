package org.gridkit.lab.orchestration;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.gridkit.vicluster.GlobOps;

public class Scopes {
    public static AnyScope any() {
        return new AnyScope();
    }
    
    public static PatternScope pattern(String pattern) {
        return new PatternScope(pattern);
    }
    
    public static Scope and(Scope left, Scope right) {
        if (left instanceof AnyScope) {
            return right;
        } else if (right instanceof AnyScope) {
            return left;
        } else {
            return new AndScope(left, right);
        }
    }
    
    public static Scope or(Scope left, Scope right) {
        if (left instanceof AnyScope) {
            return right;
        } else if (right instanceof AnyScope) {
            return left;
        } else {
            return new OrScope(left, right);
        }
    }
    
    public static class AnyScope implements Scope, Serializable {
        private static final long serialVersionUID = -4343180900004075858L;

        @Override
        public boolean contains(String node) {
            return true;
        }
    }
    
    public static abstract class AbstractScope implements Scope, Serializable {
        private static final long serialVersionUID = 8905869583721306000L;

        public AndScope and(Scope scope) {
            return new AndScope(this, scope);
        }
        
        public OrScope or(Scope scope) {
            return new OrScope(this, scope);
        }
    }
    
    public static class PatternScope extends AbstractScope {
        private static final long serialVersionUID = -2153009740049280455L;
        
        private String pattern;
        private Pattern regex;

        public PatternScope(String pattern) {
            this.pattern = pattern;
            this.regex = GlobOps.translate(pattern, ".");
        }

        @Override
        public boolean contains(String node) {
            return regex.matcher(node).matches();
        }
        
        @Override
        public String toString() {
            return "P[" + pattern + "]";
        }
    }
    
    public static class AndScope extends AbstractScope {
        private static final long serialVersionUID = 4663886816696051041L;
        
        private Scope left;
        private Scope right;
        
        public AndScope(Scope left, Scope right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean contains(String node) {
            return left.contains(node) && right.contains(node);
        }
        
        @Override
        public String toString() {
            return "And[" + left + ", " + right + "]";
        }
    }
    
    public static class OrScope extends AbstractScope {
        private static final long serialVersionUID = -8938064918336234878L;
        
        private Scope left;
        private Scope right;
        
        public OrScope(Scope left, Scope right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean contains(String node) {
            return left.contains(node) || right.contains(node);
        }
        
        @Override
        public String toString() {
            return "Or[" + left + ", " + right + "]";
        }
    }
}
