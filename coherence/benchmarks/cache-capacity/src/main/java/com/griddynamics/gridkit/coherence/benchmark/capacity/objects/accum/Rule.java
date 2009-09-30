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

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class Rule implements Serializable {

	private static final long serialVersionUID = 20090512L;
	/* POF id 10003 */
	
	public static final byte OP_VALUE = 0;
	public static final byte OP_STATUS = 1;
	
	private long id;
	private long dateFrom;	
	private long accumulatorId;
	private byte operation;
	private int value;
	
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
	
	public long getAccumulatorId() {
		return accumulatorId;
	}
	
	public void setAccumulatorId(long accumulatorId) {
		this.accumulatorId = accumulatorId;
	}
	
	public byte getOperation() {
		return operation;
	}

	public void setOperation(byte operation) {
		this.operation = operation;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
