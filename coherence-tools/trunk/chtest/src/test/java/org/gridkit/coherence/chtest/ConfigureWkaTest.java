package org.gridkit.coherence.chtest;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.RequestTimeoutException;

public class ConfigureWkaTest {

	int port1 = 12010;
	int port2 = 12020;
	int port3 = 12030;
	int port4 = 12040;

	@Rule
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	@Test
	public void test_custom_wka() {
		
		CohNode node = cloud.node("node-a");
		node.setClusterLocalHost("127.0.0.1");
		node.setClusterLocalPort(port1);
		node.setTCMPTimeout(5000);
		
		node.addWkaAddress("127.0.0.1", port1);
		
		node.ensureCluster();
	}

	@Test(expected = RequestTimeoutException.class)
	public void test_cluster_start_failure() {
		
		CohNode node = cloud.node("node-a");
		node.setClusterLocalHost("127.0.0.1");
		node.setClusterLocalPort(port1);
		node.setTCMPTimeout(5000);
		
		node.addWkaAddress("127.0.0.1", port2);
		
		node.ensureCluster();
	}

	@Test
	public void test_multiple_wka_1() {
		
		CohNode all = cloud.node("**");
		CohNode nodeA = cloud.node("node-a");
		CohNode nodeB = cloud.node("node-b");
		all.setClusterLocalHost("127.0.0.1");
		nodeA.setClusterLocalPort(port1);
		nodeB.setClusterLocalPort(port2);
		all.setTCMPTimeout(20000);
		
		all.addWkaAddress("127.0.0.1", port2);
		all.addWkaAddress("127.0.0.1", port3);
		
		all.ensureCluster();
	}

	@Test
	public void test_multiple_wka_2() {
		
		CohNode all = cloud.node("**");
		CohNode nodeA = cloud.node("node-a");
		CohNode nodeB = cloud.node("node-b");
		all.setClusterLocalHost("127.0.0.1");
		nodeA.setClusterLocalPort(port1);
		nodeB.setClusterLocalPort(port2);
		all.setTCMPTimeout(20000);
		
		all.addWkaAddress("127.0.0.1", port3);
		all.addWkaAddress("127.0.0.1", port2);
		
		all.ensureCluster();
	}
	
}
