package org.gridkit.cloudcache.core.data;

/**
 * Piece of data, usually BLOB but it is not of concern for
 * one who is using it.
 */
public interface Opaque<T> extends Comparable<T> {

	public int hashCode();
	
	public boolean equals();
	
}
