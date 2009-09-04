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
public class Accumulator implements Serializable, Cloneable {

	private static final Attribute[] EMPTY_ATTRS = new Attribute[0];
	private static final Threshold[] EMPTY_RULES = new Threshold[0];
	
	private static final long serialVersionUID = 20090512L;
	/* POF id 10001 */
	
	public static final byte STATUS_ACTIVE = 0;
	public static final byte STATUS_BLOCKED = 1;
	
	private long id;
	private long dateFrom;
	private byte status;
	private int value;
	private String side;
	private String name;
	
	private Attribute[] attributes = EMPTY_ATTRS;
	private Threshold[] thresholds = EMPTY_RULES; 
	
	public Accumulator() {		
	}

	@Override
	public Accumulator clone() {
		try {
			return (Accumulator) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Impossible");
		}
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public long getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(long dateFrom) {
		this.dateFrom = dateFrom;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getSide() {
		return side;
	}

	public void setSide(String side) {
		this.side = side;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Attribute[] getAttributes() {
		return attributes;
	}

	public void addAttribute(Attribute attrib) {
		attributes = ArrayUtils.append(attributes, attrib);
	}

	public void addAttributes(Attribute[] attribs) {
		for(Attribute attrib: attribs) {
			addAttribute(attrib);
		}
	}

	public void addAttributes(Collection<Attribute> attribs) {
		for(Attribute attrib: attribs) {
			addAttribute(attrib);
		}
	}

	public Threshold[] getThresholds() {
		return thresholds;
	}

	public void addThreshold(Threshold threshold) {
		thresholds = ArrayUtils.append(thresholds, threshold);
	}
	
	public void addThresholds(Threshold[] thresholds) {
		for(Threshold threshold: thresholds) {
			addThreshold(threshold);
		}
	}
	public void addThresholds(Collection<Threshold> thresholds) {
		for(Threshold threshold: thresholds) {
			addThreshold(threshold);
		}
	}
}
