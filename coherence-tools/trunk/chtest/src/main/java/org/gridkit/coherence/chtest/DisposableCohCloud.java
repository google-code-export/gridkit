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

import org.gridkit.vicluster.ViNodeSet;
import org.junit.rules.ExternalResource;

public class DisposableCohCloud extends ExternalResource implements CohCloudRule {

	private SimpleCohCloud cloud;
	
	public DisposableCohCloud() {
	}
	
	protected void ensureInitialzed() {
		if (cloud == null) {
			throw new IllegalStateException("Method before wasn't called yet (do you have @Rule annotation?). If you use this class outside of JUnit lifecycle, please use SimpleCohCloud instead");
		}
	}
	
	@Override
	protected void before() throws Throwable {
		cloud = new SimpleCohCloud();
	}
	
	@Override
	protected void after() {
		ensureInitialzed();
		cloud.shutdown();
	}

	@Override
	public ViNodeSet getCloud() {
		ensureInitialzed();
		return cloud.getCloud();
	}
	
	@Override
	public CohNode all() {
		ensureInitialzed();
		return cloud.all();
	}

	/**
	 * Return node by name (or group of nodes for pattern).
	 */
	@Override
	public CohNode node(String namePattern) {
		ensureInitialzed();
		return cloud.node(namePattern);
	}

	@Override
	public CohNode nodes(String... namePatterns) {
		ensureInitialzed();
		return cloud.nodes(namePatterns);
	}

	/**
	 * List non-terminated nodes matching namePattern
	 */	
	@Override
	public Collection<CohNode> listNodes(String namePattern) {
		ensureInitialzed();
		return cloud.listNodes(namePattern);
	}
	
	@Override
	public void shutdown() {
		ensureInitialzed();
		cloud.shutdown();
	}	
}
