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
import java.util.Arrays;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 * @deprecated class is for internal use, kept public to support POF deserialization 
 */
public class ValueContatiner implements PortableObject, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static int[] NO_VERSIONS = new int[0];
	private static Object[] NO_VALUES = new Object[0];
	
	private int[] versions;
	private Object[] values;
	
	public ValueContatiner() {
		versions = NO_VERSIONS;
		values = NO_VALUES;
	}
	
	public Object getLatestVersion() {
		return values[0];
	}
	
	public Object getVersionAt(int seqNo) {
		if (seqNo == Versions.LATEST_VERSION) {
			return getLatestVersion();
		}
		for (int i = 0; i != versions.length;  ++i) {
			if (versions[i] <= seqNo) {
				return values[i];
			}
		}
		return null;
	}
	
	public Object[] getAllVersions() {
		return values;
	}
	
	public void addVersion(int seqNo, Object value) {
		if (versions.length == 0) {
			versions = new int[]{seqNo};
			values = new Object[]{value};
		}
		else {
			// TOD allow to override max version?
			if (Versions.greater(versions[0], seqNo)) {
				throw new IllegalArgumentException("Version conflict: new=" + seqNo + " existing=" + versions[0]);
			}
			else if (seqNo == versions[0]) {
				values[0] = value;
			}
			else {
				int[] newVersions = new int[versions.length + 1];
				newVersions[0] = seqNo;
				System.arraycopy(versions, 0, newVersions, 1, versions.length);
				Object[] newValues = new Object[values.length + 1];
				newValues[0] = value;
				System.arraycopy(values, 0, newValues, 1, values.length);
				versions = newVersions;
				values = newValues;
			}
		}
	}
	
	public void rollback(int seqNo) {
		if (versions[0] == seqNo) {
			if (versions.length == 1) {
				versions = NO_VERSIONS;
				values = NO_VALUES;
			}
			else {
				int[] newVersions = new int[versions.length - 1];
				System.arraycopy(versions, 1, newVersions, 0, newVersions.length);
				Object[] newValues = new Object[values.length - 1];
				System.arraycopy(values, 1, newValues, 0, newValues.length);
				versions = newVersions;
				values = newValues;
			}
		}
	}
	
	public void sweep(int seqNo) {
		for (int i = 0; i < versions.length;  ++i) {			
			if (i == 0 && values[i] != null) {
				if (versions[i] <= seqNo) {
					versions[i] = Versions.BASELINE_VERSION;
				}
			}
			else if (versions[i] <= seqNo) {
				versions = Arrays.copyOf(versions, i);
				values = Arrays.copyOf(values, i);
				return;
			}
		}
	}
	
	public boolean isEmpty() {
		return versions.length == 0;
	}
	
	@Override
	public void readExternal(PofReader in) throws IOException {
		versions = in.readIntArray(1);
		values = in.readObjectArray(2, new Object[0]);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeIntArray(1, versions);
		out.writeObjectArray(2, values);
	}
}
