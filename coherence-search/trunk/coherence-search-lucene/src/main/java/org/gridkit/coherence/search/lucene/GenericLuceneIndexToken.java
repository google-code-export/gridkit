package org.gridkit.coherence.search.lucene;

import java.io.Serializable;

public class GenericLuceneIndexToken implements Serializable {

	private static final long serialVersionUID = 20100721L;
	
	public GenericLuceneIndexToken() {
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
	
	

}
