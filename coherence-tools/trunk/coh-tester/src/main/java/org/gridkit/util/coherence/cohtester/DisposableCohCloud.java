package org.gridkit.util.coherence.cohtester;

import java.util.Collection;

import org.gridkit.vicluster.ViManager;
import org.junit.rules.ExternalResource;

public class DisposableCohCloud extends ExternalResource implements CohCloudRule {

	private SimpleCohCloud cloud;
	
	public DisposableCohCloud() {
		cloud = new SimpleCohCloud();
	}
	
	@Override
	public void useLocalCluster() {
		cloud.useLocalCluster();
	}

	@Override
	public void useEmbededCluster() {
		cloud.useEmbededCluster();
	}

	@Override
	protected void after() {
		cloud.shutdown();
	}

	@Override
	public ViManager getCloud() {
		return cloud.getCloud();
	}
	
	@Override
	public CohNode all() {
		return cloud.all();
	}

	/**
	 * Return node by name (or group of nodes for pattern).
	 */
	@Override
	public CohNode node(String namePattern) {
		return cloud.node(namePattern);
	}

	@Override
	public CohNode nodes(String... namePatterns) {
		return cloud.nodes(namePatterns);
	}

	/**
	 * List non-terminated nodes matching namePattern
	 */	
	@Override
	public Collection<CohNode> listNodes(String namePattern) {
		return cloud.listNodes(namePattern);
	}
	
	@Override
	public void shutdown() {
		cloud.shutdown();
	}	
}
