package org.gridkit.lab.orchestration.script;

public class ScriptExecutionException extends RuntimeException {
    private static final long serialVersionUID = 2806542880198716437L;

    public ScriptExecutionException() {
        super();
    }

    public ScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptExecutionException(String message) {
        super(message);
    }

    public ScriptExecutionException(Throwable cause) {
        super(cause);
    }
}
