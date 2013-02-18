package org.gridkit.util.coherence.cohtester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViManager;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViProps;
import org.junit.rules.ExternalResource;

public class DisposableCohCloud extends ExternalResource implements CohCloudRule {

	private ViManager cloud;
	
	public DisposableCohCloud() {
		cloud = CloudFactory.createCloud();
		ViProps.at(cloud.node("**"))
			.setIsolateType()
			.setSilentShutdown();
	}
	
	@Override
	public void useLocalCluster() {
		ViProps.at(cloud.node("**"))
			.setLocalType();
	}

	@Override
	public void useEmbededCluster() {
		ViProps.at(cloud.node("**"))
		.setLocalType();
	}

	@Override
	protected void after() {
		cloud.shutdown();
	}

	@Override
	public ViManager getCloud() {
		return cloud;
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
