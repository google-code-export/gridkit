package org.gridkit.coherence.search.lucene;


import java.io.Serializable;

import org.junit.Ignore;

@Ignore
@SuppressWarnings("serial")
public class Document implements Serializable {

	private String text;

	public Document(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public String toString() {
		return text;
	}
}
