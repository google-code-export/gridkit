/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.txlite;

import java.io.IOException;
import java.io.Serializable;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 * @deprecated class is for internal use, kept public to support POF deserialization 
 */
public class LogEntry implements PortableObject, Serializable {

	private static final long serialVersionUID = 20110407L;
	
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
