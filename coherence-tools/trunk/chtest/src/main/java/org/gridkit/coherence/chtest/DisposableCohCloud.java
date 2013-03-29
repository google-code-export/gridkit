/**
 * Copyright 2013 Alexey Ragozin
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
package org.gridkit.coherence.chtest;

import java.util.Collection;

import org.gridkit.vicluster.ViManager;
import org.junit.rules.ExternalResource;

public class DisposableCohCloud extends ExternalResource implements CohCloudRule {

	private SimpleCohCloud cloud;
	
	public DisposableCohCloud() {
		cloud = new SimpleCohCloud();
	}

	
	
	@Override
	protected void before() throws Throwable {
		cloud = new SimpleCohCloud();
	}

	@Override
	protected void after() {
		cloud.shutdown();
	}

	@Override
	public ViManager getCloud() {
		if (cloud == null) {
			throw new IllegalStateException("Should be used with @Rule annotation");
		}
		return cloud.getCloud();
	}
	
	@Override
	public CohNode all() {
		if (cloud == null) {
			throw new IllegalStateException("Should be used with @Rule annotation");
		}
		return cloud.all();
	}

	/**
	 * Return node by name (or group of nodes for pattern).
	 */
	@Override
	public CohNode node(String namePattern) {
		if (cloud == null) {
			throw new IllegalStateException("Should be used with @Rule annotation");
		}
		return cloud.node(namePattern);
	}

	@Override
	public CohNode nodes(String... namePatterns) {
		if (cloud == null) {
			throw new IllegalStateException("Should be used with @Rule annotation");
		}
		return cloud.nodes(namePatterns);
	}

	/**
	 * List non-terminated nodes matching namePattern
	 */	
	@Override
	public Collection<CohNode> listNodes(String namePattern) {
		if (cloud == null) {
			throw new IllegalStateException("Should be used with @Rule annotation");
		}
		return cloud.listNodes(namePattern);
	}
	
	@Override
	public void shutdown() {
		if (cloud == null) {
			throw new IllegalStateException("Should be used with @Rule annotation");
		}
		cloud.shutdown();
	}	
}
