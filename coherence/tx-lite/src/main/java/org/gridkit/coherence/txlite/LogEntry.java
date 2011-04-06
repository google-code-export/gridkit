package org.gridkit.coherence.txlite;

import java.io.IOException;
import java.io.Serializable;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class LogEntry implements PortableObject, Serializable {

	private String cacheName;
	private Object key;
	private int version;
	
	public LogEntry() {
		// deserialization construction
	}
	
	public LogEntry(String cacheName, Object key, int version) {
		this.cacheName = cacheName;
		this.key = key;
		this.version = version;
	}

	public String getCacheName() {
		return cacheName;
	}

	public Object getKey() {
		return key;
	}

	public int getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cacheName == null) ? 0 : cacheName.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + version;
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
		LogEntry other = (LogEntry) obj;
		if (cacheName == null) {
			if (other.cacheName != null)
				return false;
		} else if (!cacheName.equals(other.cacheName))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		cacheName = in.readString(1);
		key = in.readObject(2);
		version = in.readInt(3);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeString(1, cacheName);
		out.writeObject(2, key);
		out.writeInt(3, version);
	}

	public String toString() {
		return cacheName + "[" + key + "]@" + version;
	}
}
