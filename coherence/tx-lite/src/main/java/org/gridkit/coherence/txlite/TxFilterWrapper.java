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
import com.tangosol.util.Filter;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 * @deprecated class is for internal use, kept public to support POF deserialization 
 */
public class TxFilterWrapper implements Filter, PortableObject, Serializable {

	private static final long serialVersionUID = 20110407L;
	
	protected Filter nested;
	protected int readVersion;
	
	public TxFilterWrapper(Filter nested, int readVesrion) {
		this.nested = nested;
		this.readVersion = readVesrion;
	}

	@Override
	public boolean evaluate(Object value) {
		if (value instanceof ValueContatiner) {
			value = ((ValueContatiner)value).getVersionAt(readVersion);
		}
		return nested.evaluate(value);
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		nested = (Filter) in.readObject(1);
		readVersion = in.readInt(2);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeObject(1, nested);
		out.writeInt(2, readVersion);
	}
}
