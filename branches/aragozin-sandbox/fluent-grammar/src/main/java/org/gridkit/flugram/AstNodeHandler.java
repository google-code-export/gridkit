package org.gridkit.flugram;


public interface AstNodeHandler<T> {

	/**
	 * Evaluates AST node and returns produced object.
	 * Call to this method terminates node life cycle.
	 */
	public T evaluate();
}
