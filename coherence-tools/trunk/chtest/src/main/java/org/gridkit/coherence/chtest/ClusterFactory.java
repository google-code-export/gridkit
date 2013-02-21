package org.gridkit.coherence.chtest;

import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViManager;
import org.gridkit.vicluster.ViProps;

public class ClusterFactory {

	public static ViManager createEmbededTestCluster() {
		ViManager cloud = CloudFactory.createCloud();
		ViProps.at(cloud.node("**")).setIsolateType();
		return cloud;
	}

	public static ViManager createLocalTestCluster() {
		ViManager cloud = CloudFactory.createCloud();
		ViProps.at(cloud.node("**")).setLocalType();
		return cloud;
	}
	
}
