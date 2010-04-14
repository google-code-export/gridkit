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
 */
package com.griddynamics.convergence.demo.dar.service;

@SuppressWarnings("serial")
public class DefaultExperimentSetup implements ExperimentSetup {

	private String id;
	private String name;
	private String color;
	private GridSchedulerProvider scheduler;
	private TradeStorageProvider storage;
	private DataAccessStrategy strategy;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setScheduler(GridSchedulerProvider scheduler) {
		this.scheduler = scheduler;
	}

	public void setStorage(TradeStorageProvider storage) {
		this.storage = storage;
	}

	public void setStrategy(DataAccessStrategy strategy) {
		this.strategy = strategy;
	}

	public String getName() {
		return name;
	}
	
	public String getColor() {
		return color;
	}

	public GridSchedulerProvider getGridProvider() {
		return scheduler;
	}

	public TradeStorageProvider getStorageProvider() {
		return storage;
	}

	public DataAccessStrategy getStrategy() {
		return strategy;
	}
}
