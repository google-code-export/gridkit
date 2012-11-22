package org.gridkit.lab.zktest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.BadVersionException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.gridkit.lab.jvm.attach.PatternJvmMatcher;
import org.gridkit.nimble.driver.Activity;
import org.gridkit.nimble.driver.ExecutionDriver;
import org.gridkit.nimble.driver.ExecutionDriver.ExecutionConfig;
import org.gridkit.nimble.driver.ExecutionHelper;
import org.gridkit.nimble.driver.MeteringDriver;
import org.gridkit.nimble.driver.PivotMeteringDriver;
import org.gridkit.nimble.metering.DTimeReporter;
import org.gridkit.nimble.metering.Measure;
import org.gridkit.nimble.metering.RawSampleCollector;
import org.gridkit.nimble.monitoring.MonitoringStack;
import org.gridkit.nimble.monitoring.ProcessCpuMonitoring;
import org.gridkit.nimble.monitoring.StandardSamplerReportBundle;
import org.gridkit.nimble.monitoring.SysPropSchemaConfig;
import org.gridkit.nimble.orchestration.ScenarioBuilder;
import org.gridkit.nimble.orchestration.TimeLine;
import org.gridkit.nimble.pivot.Pivot;
import org.gridkit.nimble.pivot.PivotReporter;
import org.gridkit.nimble.pivot.display.DisplayBuilder;
import org.gridkit.nimble.pivot.display.PivotPrinter2;
import org.gridkit.nimble.print.PrettyPrinter;
import org.gridkit.nimble.probe.probe.Monitoring;
import org.gridkit.nimble.probe.probe.MonitoringDriver;
import org.gridkit.nimble.util.ConfigurationTemplate;
import org.gridkit.util.concurrent.FutureBox;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViNodeSet;

public class ZkBench {

	private MonitoringStack mstack = new MonitoringStack();
	
	@SuppressWarnings("serial")
	public static class ZkBenchConfig extends ConfigurationTemplate {
		
		public long testTime = 60000;

		public int readerCount = 4;
		public int writerCount = 4;

		public double singleReaderRate = 50; 
		public double singleWriterRate = 20; 
		
		ExecutionConfig getReaderExecConfig() {
			return ExecutionHelper.constantRateExecution(singleReaderRate, 10, true);
		}
		
		ExecutionConfig getWriteExecConfig() {
			return ExecutionHelper.constantRateExecution(singleWriterRate, 10, true);
		}
	}
	
	private final ZkBenchConfig config;
	private boolean dumpLevels = false;
	private String csvFileName;
	private String rawSampleFile;
	
	
	public ZkBench(ZkBenchConfig config) {
		this.config = config;	
		
		addReporting();
	}
	
	public void setSummaryCsvPath(String path) {
		csvFileName = path;
	}

	public void setRawCsvPath(String path) {
		rawSampleFile = path;
	}

	public void dumpRawLevels() {
		dumpLevels = true;
	}

	public void addReporting() {
		StandardSamplerReportBundle mon = new StandardSamplerReportBundle("sampler");		
		mon.sortByField(Measure.NAME);		
		mstack.addBundle(mon, "Operation statistics");
	}

	public void addCpuReporting() {
		PatternJvmMatcher matcher = new PatternJvmMatcher();
		matcher.matchVmName(".*boot.*"); 
		matcher.matchProp("proc-type", ".*");
		
		SysPropSchemaConfig.ProcessId config = new SysPropSchemaConfig.ProcessId();
		config.readProp("proc-type", "Process type");
		
		ProcessCpuMonitoring mon = new ProcessCpuMonitoring("proc-cpu");
		mon.setLocator(matcher);
		mon.setSchemaConfig(config);
		mon.groupBy("Process type");
		mon.sortByField("Process type");
		DisplayBuilder.with(mon)
		.attribute("Process type");
		mstack.addBundle(mon, "Process CPU utilization");
	}

	public void startZooKeeper(ViNodeSet cloud) {
		
		ZooEnsemble ensemble = new ZooEnsemble();
		ensemble.setBaseZookeeperPort(40000);
		ensemble.setBaseZookeeperPath("{temp}/zk-test");
		
		for(ViNode node: cloud.listNodes("**.ZK.**")) {
			ensemble.addToEnsemble(node);
		}
		
		ensemble.startEnsemble();
		
		String uri = ensemble.getConnectionURI();
		cloud.node("**").setProp("zooConnection", uri);
		
	}
	
	public void perfrom(ViNodeSet cloud) {
		startZooKeeper(cloud);
		PivotReporter reporter = runTest(cloud);
		
		if (dumpLevels) {
			System.out.println();
			PivotPrinter2 pp = new PivotPrinter2();
			pp.dumpUnprinted();
			new PrettyPrinter().print(System.out, pp.print(reporter.getReader()));			
		}
		
		mstack.printSections(System.out, reporter);
		if (csvFileName != null) {
			try {
				mstack.reportToCsv(csvFileName, reporter);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public PivotReporter runTest(ViNodeSet cloud) {
		
		ScenarioBuilder sb = new ScenarioBuilder();
		
		Pivot pivot = new Pivot();
		mstack.configurePivot(pivot);
		PivotMeteringDriver pd = new PivotMeteringDriver(pivot);
		pd.setKeyRawSampler(rawSampleFile != null);
		
		MeteringDriver md = sb.deploy(pd);
		mstack.inject(MeteringDriver.class, md);
		mstack.inject(MonitoringDriver.class, Monitoring.deployDriver("**.MON.**", sb, md));
		
		sb.checkpoint("init");
		sb.checkpoint("start");
		sb.sleep(config.testTime);
		sb.checkpoint("stop");
		sb.checkpoint("done");

		mstack.deploy(sb, new TimeLine("init", "start", "stop", "done"));
		
		deployTest(sb, md);
		
		sb.from("done");
		md.flush();
		
		RawSampleCollector rawCollector = null;
		if (rawSampleFile != null) {
			try {
				rawCollector = new RawSampleCollector();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			md.dumpRawSamples(rawCollector, 16 << 10);
		}
		
		sb.debug_simulate();
		
		sb.getScenario().play(cloud);
		
		if (rawSampleFile != null) {
			try {
				System.out.println("Dumping raw samples [" + rawSampleFile + "]");
				FileWriter fw = new FileWriter(new File(rawSampleFile));
				fw.write("sep=;\n");
				rawCollector.writeCsv(fw);
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return pd.getReporter();		
	}

	private void deployTest(ScenarioBuilder sb, MeteringDriver md) {
		
		sb.fromStart();		
		TestDriver driver = new TestDriverImpl();
		driver = sb.deploy("**.WORKER.**", driver);
		ExecutionDriver exec = sb.deploy(ExecutionHelper.newDriver());
		
		for(int i = 0; i != config.readerCount; ++i) {
			sb.from("init");
			Runnable r = driver.getReader(md);
			sb.sleep(5000);
			sb.join("start");
			sb.from("start");
			Activity act = exec.start(r, config.getReaderExecConfig(), null);
			sb.from("stop");
			act.stop();
			sb.fromStart();
			act.join();
			sb.join("done");			
		}

		for(int i = 0; i != config.writerCount; ++i) {
			sb.from("init");
			Runnable r = driver.getWriter(md);
			sb.join("start");
			sb.from("start");
			Activity act = exec.start(r, config.getWriteExecConfig(), null);
			sb.from("stop");
			act.stop();
			sb.fromStart();
			act.join();
			sb.join("done");			
		}
	}
	
	public static interface TestDriver {
	
		public Runnable getReader(MeteringDriver metering);
		
		public Runnable getWriter(MeteringDriver metering);
		
	}
	
	@SuppressWarnings("serial")
	public static class TestDriverImpl implements TestDriver, Serializable {

		enum Op { hit, miss, create, update, fail };
		
		private String basePath = "/test";
		private int nameRange = 1000;

		private synchronized ZooKeeper connect() {
			// synchronization will limit concurrent connection attempts
			// it seems that something is broken with NIO accept performance on Windows
			String uri = System.getProperty("zooConnection");
			while(true) {
				final FutureBox<Void> connection = new FutureBox<Void>();
				ZooKeeper client = null;
				try {
					client = new ZooKeeper(uri, 120000, new Watcher() {
						@Override
						public void process(WatchedEvent event) {
							System.out.println(event);
							connection.setData(null);
						}
					});
					final List<ACL> acl = new ArrayList<ACL>();
					acl.add(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE));
					if (client.exists(basePath, false) == null) {
						try {
							client.create(basePath, new byte[0], acl, CreateMode.PERSISTENT);
						}
						catch(NodeExistsException e) {
							// ignore
						}
					}
				} catch (ConnectionLossException e) {
					try {
						client.close();
					}
					catch(Exception ee) {
						// ignore
					}
					continue;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
				return client;
			}
		}
		
		@Override
		public Runnable getReader(final MeteringDriver metering) {
			final ZooKeeper client;
			client = connect();
			
			return new Runnable() {
				
				DTimeReporter<Op> rep = metering.samplerBuilder().timeReporter("Read (%s)", Op.class);
				Random rand = new Random();
				
				@Override
				public void run() {
				
					try {
						int n = rand.nextInt(nameRange);
						String path = basePath + "/node-" + n;
						DTimeReporter.StopWatch<Op> sw = rep.start();
						try {
							byte[] data = client.getData(path, false, null);
							n = data.length; // just to avoid warning
							sw.stop(Op.hit);
						}
						catch(NoNodeException e) {
							sw.stop(Op.miss);
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
		}

		@Override
		public Runnable getWriter(final MeteringDriver metering) {
			final ZooKeeper client;
			client = connect();
			
			return new Runnable() {
				
				DTimeReporter<Op> rep = metering.samplerBuilder().timeReporter("Write (%s)", Op.class);
				Random rand = new Random();
				
				@Override
				public void run() {
				
					List<ACL> acl;
					acl = new ArrayList<ACL>();
					acl.add(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE));
					
					try {
						int n = rand.nextInt(nameRange);
						byte[] data = new byte[128];
						rand.nextBytes(data);
						
						String path = basePath + "/node-" + n;
						DTimeReporter.StopWatch<Op> sw = rep.start();
						Stat st = client.exists(path, false);
						if (st == null) {
							try {
								client.create(path, data, acl, CreateMode.PERSISTENT);
								sw.stop(Op.create);
							}
							catch(NodeExistsException e) {
								sw.stop(Op.fail);
								return;								
							}
						}
						else {
							try {
								client.setData(path, data, st.getVersion());
								sw.stop(Op.update);
							}
							catch(BadVersionException e) {
								sw.stop(Op.fail);
								return;								
							}
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
		}
	}	
}
