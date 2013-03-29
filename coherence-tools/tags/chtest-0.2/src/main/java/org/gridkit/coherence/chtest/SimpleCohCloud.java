package org.gridkit.coherence.chtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViManager;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViProps;

public class SimpleCohCloud implements CohCloud {

	private ViManager cloud;
	
	public SimpleCohCloud() {
		cloud = CloudFactory.createCloud();
		ViProps.at(cloud.node("**"))
			.setIsolateType()
			.setSilentShutdown();
		CohHelper.enableCoherenceThreadKillers(cloud.node("**"), true);
		CohHelper.enableViNodeName(cloud.node("**"), true);
	}
	
	@Override
	public ViManager getCloud() {
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
	}	
}
