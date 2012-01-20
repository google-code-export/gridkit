/**
 * Copyright 2011 Alexey Ragozin
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
package org.gridkit.utils.vicluster;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridkit.coherence.util.classloader.Isolate;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ViCluster implements ViProps {

	private String clusterName;
	private String[] packages;
	
	private Map<String, ViNode> nodes = new HashMap<String, ViNode>();
	
	private Map<String, String> props = new HashMap<String, String>();
	private List<URL> incClasspath = new ArrayList<URL>();
	private List<URL> excClasspath = new ArrayList<URL>();
	
	public ViCluster(String name, String... packages) {
		this.clusterName = name;
		this.packages = packages;
	}

	public String getClusterProp(String prop) {
		return props.get(prop);
	}
	
	@Override
	public void setProp(String prop, String value) {
		props.put(prop, value);
		for(ViNode node: nodes.values()) {
			node.setProp(prop, value);
		}
	}

	public void addToClasspath(URL url) {
		incClasspath.add(url);
		for(ViNode node: nodes.values()) {
			node.addToClasspath(url);
		}
	}

	public void removeFromClasspath(URL url) {
		excClasspath.add(url);
		for(ViNode node: nodes.values()) {
			node.removeFromClasspath(url);
		}
	}
	
	public ViNode node(String name) {
		if (!nodes.containsKey(name)) {
			createViNode(name);
		}
		return nodes.get(name);
	}
	
	private void createViNode(String name) {
		Isolate is = new Isolate(clusterName + "." + name, packages);
		is.setProp(props);
		for(URL u: incClasspath) {
			is.addToClasspath(u);
		}
		for(URL u: excClasspath) {
			is.removeFromClasspath(u);
		}
		
		ViNode vnode = new ViNode(this, name, is);
		nodes.put(name, vnode);
	}
	
	void remove(ViNode node) {
		nodes.values().remove(node);
	}
	
	public void shutdown() {
		for(ViNode node: new ArrayList<ViNode>(nodes.values())) {
			node.shutdown();
		}
		nodes.clear();
		for(int i = 0; i != 3; ++i) {
			System.gc();
			Runtime.getRuntime().runFinalization();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}

	public void kill() {
		for(ViNode node: new ArrayList<ViNode>(nodes.values())) {
			node.kill();
		}
	}

	public void setProp(Map<String, String> props) {
		this.props.putAll(props);
		for(ViNode node: nodes.values()) {
			node.setProp(props);
		}
	}
}
