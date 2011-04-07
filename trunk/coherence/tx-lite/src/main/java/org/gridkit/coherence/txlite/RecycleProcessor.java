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
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 * @deprecated class is for internal use, kept public to support POF deserialization 
 */
public class RecycleProcessor extends AbstractProcessor implements PortableObject, Serializable {

	private static final long serialVersionUID = 20110402L;
	
	private int targetVersion;
	
	public RecycleProcessor() {
		// for deserialization
	}
	
	public RecycleProcessor(int targetVersion) {
		this.targetVersion = targetVersion;
	}
	
	@Override
	public Object process(Entry entry) {
		ValueContatiner cx = (ValueContatiner) entry.getValue();
		if (cx != null) {
			cx.sweep(targetVersion);
			if (cx.isEmpty()) {
				entry.remove(false);
			}
			else {
				entry.setValue(cx);
			}
		}
		return null;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		targetVersion = in.readInt(1);		
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeInt(1, targetVersion);		
	}
}
