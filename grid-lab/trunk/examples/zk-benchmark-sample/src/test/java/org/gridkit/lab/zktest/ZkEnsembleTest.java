package org.gridkit.lab.zktest;

import org.gridkit.lab.zktest.ZooEnsembleBuilder.ZooEnsembleDriver;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.nimble.driver.Activity;
import org.gridkit.nimble.orchestration.ScenarioBuilder;
import org.gridkit.vicluster.ViNodeSet;
import org.gridkit.vicluster.ViProps;
import org.junit.Test;

public class ZkEnsembleTest {

	public ViNodeSet createCloud() {
		
		ViNodeSet cloud = CloudFactory.createCloud();
		ViProps.at(cloud.node("**")).setIsolateType();
		cloud.nodes("ZK.1", "ZK.2", "ZK.3");
		
		return cloud;		
	}
	
	@Test
	public void zooRunTest() {
		
		ViNodeSet cloud = createCloud();
		
		ScenarioBuilder sb = new ScenarioBuilder();
		sb.checkpoint("init");
		sb.checkpoint("start");
		sb.sleep(15000);
		sb.checkpoint("stop");
		sb.checkpoint("done");
		
		ZooEnsembleDriver driver = ZooEnsembleBuilder.build()
				.baseDataDir("target/zkdata")
				.driver();
		
		sb.from("init");
		driver = sb.deploy("**.ZK.**", driver);
		driver.init();
		sb.join("start");
		sb.from("start");
		Activity zk = driver.start();
		zk.join();
		sb.join("done");
		sb.from("stop");
		zk.stop();
		
//		sb.debug_simulate();
		sb.getScenario().play(cloud);
	}
	
	
}
