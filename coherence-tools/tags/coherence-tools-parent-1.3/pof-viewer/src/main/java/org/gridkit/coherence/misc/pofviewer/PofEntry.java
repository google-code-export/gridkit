package org.gridkit.coherence.misc.pofviewer;

import java.io.Serializable;

public class PofEntry implements Serializable {

	private static final long serialVersionUID = 20120523L;
	
	private PofPath path;
	private int typeId;
	private Object value; // null for composite objects

	public PofEntry(PofPath path, int typeId, Object value) {
		this.path = path;
		this.typeId = typeId;
		this.value = value;
	}

	public PofPath getPath() {
		return path;
	}

	public int getTypeId() {
		return typeId;
	}

	public Object getValue() {
		return value;
	}
	
	public String toString() {
		return "{" + path + "(" + typeId + ") " + value + "}";
	}
	
}
