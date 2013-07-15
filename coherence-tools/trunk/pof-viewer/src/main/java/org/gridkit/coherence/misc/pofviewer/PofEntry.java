/**
 * Copyright 2012 Alexey Ragozin
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