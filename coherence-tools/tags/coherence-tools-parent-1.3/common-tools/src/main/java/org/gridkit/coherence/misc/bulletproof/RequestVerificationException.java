package org.gridkit.coherence.misc.bulletproof;

public class RequestVerificationException extends Exception {

	private static final long serialVersionUID = 20120427L;

	public RequestVerificationException() {
		super();
	}

	public RequestVerificationException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestVerificationException(String message) {
		super(message);
	}

	public RequestVerificationException(Throwable cause) {
		super(cause);
	}
}
