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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViConfigurable;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViProps;

public class SimpleCohCloud implements CohCloud {

	private Cloud cloud;

	/**
	 * Present configuration for Coherence cluster test.
	 * <li>Default node type - isolate</li>
	 * <li>Silent shutdown enables</li>
	 * <li>Coherence specific thread killers</li>
	 * <li>Node name is used instead if PID in cluster member description</li>
	 */
	public static void applyDefaultConfig(ViConfigurable node) {
		ViProps.at(node)
			.setIsolateType()
			.setSilentShutdown();
		CohHelper.enableCoherenceThreadKillers(node, true);
		CohHelper.enableViNodeName(node, true);		
	}
	
	/**
	 * Wrap existing cloud with {@link CohCloud} syntactic sugar.
	 * <b>Does not apply default config for nodes</b>
	 */
	public SimpleCohCloud(Cloud cloud) {
		this.cloud = cloud;
	}
	
	/**
	 * Create new cloud and configure it with {@link #applyDefaultConfig(ViConfigurable)} settings.
	 */
	public SimpleCohCloud() {
		this(CloudFactory.createCloud());
		applyDefaultConfig(cloud.node("**"));
	}
	
	@Override
	public Cloud getCloud() {
		return cloud;
	}
	
	public CohNode all() {
		return node("**");
	}
	
	/**
	 * Return node by name (or group of nodes for pattern).
	 */
	@Override
	public CohNode node(String namePattern) {
		return new NodeWrapper(cloud.node(namePattern));
	}

	@Override
	public CohNode nodes(String... namePatterns) {
		return new NodeWrapper(cloud.nodes(namePatterns));
	}

	/**
	 * List non-terminated nodes matching namePattern
	 */	
	@Override
	public Collection<CohNode> listNodes(String namePattern) {
		List<CohNode> list = new ArrayList<CohNode>();
		for(ViNode node: cloud.listNodes(namePattern)) {
			list.add(new NodeWrapper(node));
		}
		return list;
	}
	
	@Override
	public void shutdown() {
		cloud.shutdown();
		System.gc();
		if (Boolean.FALSE.booleanValue()) {
			pushPerm();
		}
		System.gc();
		
	}	
	
	private void pushPerm() {
		List<String> bloat = new ArrayList<String>();
		int spree = 0;
		int n = 0;
		while(spree < 5) {
			try {
				byte[] b = new byte[1 << 20];
				Arrays.fill(b, (byte)('A' + ++n));
				bloat.add(new String(b).intern());
				spree = 0;
			}
			catch(OutOfMemoryError e) {
				++spree;
				System.gc();
			}
		}
		return;
	}	
}
