package org.gridkit.coherence.search.timeseries;

import java.io.Serializable;

import org.junit.Ignore;

import com.tangosol.net.cache.KeyAssociation;

@Ignore
@SuppressWarnings("serial")
public class SampleKey implements Serializable, KeyAssociation {

	private Object sKey;
	private int ord;
	
	public SampleKey(Object sKey, int ord) {
		this.sKey = sKey;
		this.ord = ord;
	}

	@Override
	public Object getAssociatedKey() {
		return sKey;
	}

	public Object getSerieKey() {
		return sKey;
	}

	public int getOrd() {
		return ord;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ord;
		result = prime * result + ((sKey == null) ? 0 : sKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SampleKey other = (SampleKey) obj;
		if (ord != other.ord)
			return false;
		if (sKey == null) {
			if (other.sKey != null)
				return false;
		} else if (!sKey.equals(other.sKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(").append(sKey).append(" v").append(ord).append(")");
		return builder.toString();
	}
}
