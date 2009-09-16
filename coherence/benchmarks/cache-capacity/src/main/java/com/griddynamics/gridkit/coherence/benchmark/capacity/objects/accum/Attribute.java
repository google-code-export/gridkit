/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
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
 */package com.griddynamics.gridkit.coherence.benchmark.capacity.objects.accum;

import java.io.Serializable;
import java.util.Collection;

import com.griddynamics.gridkit.coherence.benchmark.capacity.utils.ArrayUtils;

/**
 * @author Alexey Ragozin (aragozin@griddynamics.com)
 */
public class Attribute implements Serializable {

	private static final long serialVersionUID = 20090512L;
	/* POF id 10002 */
	
	public static final int TYPE_SINGLE = 0;
	public static final int TYPE_LIST = 1;
	
	private static final String[] EMPTY_VALUES = new String[0];
	
	private String name;
	private byte type = TYPE_SINGLE;
	private String[] values = EMPTY_VALUES;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public byte getType() {
		return type;
	}
	
	public void setType(byte type) {
		this.type = type;
	}
	
	public String[] getValues() {
		return values;
	}
	
	public void addValue(String value) {
		values = ArrayUtils.append(values, value);
		this.type = TYPE_LIST;
	}

	public void addValues(String[] values) {
		for(String value: values) {
			addValue(value);
		}
	}

	public void addValues(Collection<String> values) {
		if (values != null) {
			for(String value: values) {
				addValue(value);
			}
		}
	}
	
	public String getValuesString() {
		StringBuffer buf = new StringBuffer();
		for(String val: values) {
			buf.append(val).append("_");
		}
		if (buf.length() > 0) {
			buf.setLength(buf.length() - 1);
		}
		return buf.toString();
	}	
}
