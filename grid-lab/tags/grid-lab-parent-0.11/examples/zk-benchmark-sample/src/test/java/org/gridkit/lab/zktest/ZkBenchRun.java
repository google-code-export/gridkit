package org.gridkit.lab.zktest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.gridkit.lab.zktest.ZkBench.ZkBenchConfig;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViManager;
import org.junit.After;
import org.junit.Test;

public class ZkBenchRun {

	private ViManager cloud = CloudFactory.createLocalCloud();
	
	@After
	public void dropCloud() {
		cloud.shutdown();
	}

	@Test
	public void start_only() {
		cloud.nodes("ZK.1", "ZK.2", "ZK.3");
//		ViProps.at(cloud.node("ZK.1")).setInProcessType();

		new ZkBench(new ZkBenchConfig()).startZooKeeper(cloud);
	}

	@Test
	public void start_and_run_locally() throws IOException {
		cloud.nodes("ZK.1", "ZK.2", "ZK.3");
		cloud.nodes("WORKER.1", "WORKER.2", "WORKER.3");
		cloud.nodes("MON");
		cloud.node("ZK.**").setProp("proc-type", "ZooServer");
		cloud.node("WORKER.**").setProp("proc-type", "Worker");
		cloud.node("**").touch();
		
		ZkBenchConfig config = new ZkBenchConfig();
		config.testTime = TimeUnit.SECONDS.toMillis(30);
		
		ZkBench bench = new ZkBench(config);
		bench.dumpLevels = true;
		
		bench.addCpuReporting();
		bench.setSummaryCsvPath("zk-local.csv");
		bench.setRawCsvPath("raw-local.csv");
		
		bench.perfrom(cloud);
	}

	@Test
	public void start_and_run_on_cluster() throws IOException {
		cloud = CloudFactory.createSshCloud("~/nanocloud-testcluster.viconf");
		
		for(String host: new String[]{"host1", "host2", "host3"}) {
			cloud.node(host + ".ZK.1");
			cloud.node(host + ".ZK.2");
			cloud.node(host + ".ZK.3");
			cloud.node(host + ".WORKER");
			cloud.node(host + ".MON");
		}

		cloud.node("**.ZK.**").setProp("proc-type", "ZooServer");
		cloud.node("**.WORKER.**").setProp("proc-type", "Worker");
		cloud.node("**").touch();
		
		ZkBenchConfig config = new ZkBenchConfig();
		config.testTime = TimeUnit.SECONDS.toMillis(300);
		config.singleReaderRate = 100;
		config.readerCount = 20;
		
		ZkBench bench = new ZkBench(config);
		bench.addCpuReporting();
		bench.setSummaryCsvPath("zk-remote.csv");
		bench.setRawCsvPath("raw-remote." + new SimpleDateFormat("yyMMdd-HHmmss").format(new Date())+ ".csv");
		
		bench.perfrom(cloud);
	}
}
