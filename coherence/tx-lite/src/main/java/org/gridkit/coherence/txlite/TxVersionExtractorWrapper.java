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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.MultiExtractor;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 * @deprecated class is for internal use, kept public to support POF deserialization 
 */
public class TxVersionExtractorWrapper implements ValueExtractor, PortableObject, Serializable {

	private static final long serialVersionUID = 20110407L;

	private ValueExtractor nested;
	private int readVersion;
	
	public TxVersionExtractorWrapper() {
		// for deserialization
	}
	
	public TxVersionExtractorWrapper(ValueExtractor extractor, int readVersion) {
		this.nested = extractor;
		this.readVersion = readVersion;
	}
	
	public Object getValueExtractor() {
		return nested;
	}

	@Override
	public Object extract(Object object) {
		if (object instanceof ValueContatiner) {
			ValueContatiner vc = (ValueContatiner) object;
			Set<Object> extracts = new HashSet<Object>();
			for(Object val : vc.getAllVersions()) {
				if (val != null) {
					Object extract = nested.extract(val);
					add(extracts, extract);
				}
			}
			if (!extracts.isEmpty()) {
				return extracts;
			}
			else {
				return Collections.EMPTY_SET;
			}
		}
		else {
			return nested.extract(object);
		}
	}

	private void add(Set<Object> extracts, Object extract) {
		if (extract instanceof Collection<?> && !(nested instanceof MultiExtractor)) {
			extracts.addAll((Collection<?>)extract);
		}
		else {
			extracts.add(extract);
		}		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nested == null) ? 0 : nested.hashCode());
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
		TxVersionExtractorWrapper other = (TxVersionExtractorWrapper) obj;
		if (nested == null) {
			if (other.nested != null)
				return false;
		} else if (!nested.equals(other.nested))
			return false;
		return true;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		nested = (ValueExtractor) in.readObject(1);		
		readVersion = in.readInt(2);		
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeObject(1, nested);
		out.writeObject(2, readVersion);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TX{").append(nested).append("}@").append(readVersion);
		return builder.toString();
	}
}
