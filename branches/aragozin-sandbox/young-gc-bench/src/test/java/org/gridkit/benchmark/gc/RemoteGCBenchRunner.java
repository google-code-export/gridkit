package org.gridkit.benchmark.gc;

import java.util.Map;
import java.util.concurrent.Callable;

import org.gridkit.benchmark.gc.YoungGCPauseBenchmark.TestResult;
import org.gridkit.lab.data.Sample;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViNodeSet;
import org.gridkit.vicluster.telecontrol.jvm.JvmProps;
import org.gridkit.vicluster.telecontrol.ssh.RemoteNodeProps;

public class RemoteGCBenchRunner implements DataPointExecutor {
	
	public static String JVM = "jvm";
	public static String HEAP_OLD = "heap.old";
	public static String HEAP_NEW = "heap.new";
	public static String DRYMODE = "dry-mode";
	public static String GC_THREADS = "gc.threads";
	public static String GC_ALGO = "gc.algo";
	public static String GC_ALGO__CMS = "cms";
	public static String GC_ALGO__G1 = "g1";
	public static String GC_ALGO__PMSC = "pmsc";
	public static String GC_STRIDES = "gc.strides";
	public static String COOPS = "coops";

	public static String PAUSE_AVG = "pause.avg";
	public static String PAUSE_SDEV = "pause.sdev";
	public static String PAUSE_COUNT = "pause.count";
	public static String PAUSE_TOTAL = "pause.total";
	public static String PAUSE_SQTOTAL = "pause.sqtotal";
	
	private ViNodeSet cloud;
	private String nodeName;
	private Map<String, String> vmpathMapping;
	private int counter = 0;
	private int defaultYoung = 64 << 20; // 
	
	public RemoteGCBenchRunner(ViNodeSet cloud, String nodeName, Map<String, String> vmpathMapping) {
		this.cloud = cloud;
		this.nodeName = nodeName;
		this.vmpathMapping = vmpathMapping;
	}

	@Override
	public synchronized Sample process(Sample coordinates) {
		Sample result = coordinates.clone();
		result.setResult("host", nodeName);
		
		ViNode node = cloud.node(nodeName + ".run-" + (counter++));

		// heartbeat timeout
		JvmProps.at(node).addJvmArg("-Dorg.gridkit.telecontrol.slave.heart-beat-timeout=3600000");
		
		configure(node, result);
		
		node.touch();
		
		TestResult tr = node.exec(new Callable<YoungGCPauseBenchmark.TestResult>(){

			@Override
			public YoungGCPauseBenchmark.TestResult call() throws Exception {
				YoungGCPauseBenchmark bench = new YoungGCPauseBenchmark();
				bench.dryMode = "true".equals(System.getProperty("drymode"));
				bench.maxTime = 1500;
				bench.minYoung = 200;
				bench.maxYoung = 400;
				bench.headRoom = 512;
				return bench.benchmark();
			}
		});
		
		try {
			node.shutdown();
		}
		catch(Exception e) {
			// ignore
		}
		
		result.setResult(PAUSE_AVG, tr.getAverage());
		result.setResult(PAUSE_SDEV, tr.getStdDev());
		result.setResult(PAUSE_COUNT, tr.youngGcCount);
		result.setResult(PAUSE_TOTAL, tr.totalTime);
		result.setResult(PAUSE_SQTOTAL, tr.totalSquareTime);
		
		return result;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + nodeName + "]";
	}

	private void configure(ViNode node, Sample dataPoint) {
		String jvm =  vmpathMapping.get(dataPoint.get(JVM));
		if (jvm == null) {
			throw new IllegalArgumentException("Unknown JVM: " + dataPoint.get(JVM));
		}
		
		long youngSize = (dataPoint.get(HEAP_NEW) == null) ? defaultYoung  : (long)(dataPoint.getDouble(HEAP_NEW) * (1l	 << 20));
		long oldSize = (long)(dataPoint.getDouble(HEAP_OLD) * (1l << 30));
		
		long fullSizeMiB = (youngSize + oldSize) >> 20;
		long youngSizeMiB = youngSize >> 20;
		
		String memCmd = "|-Xmx" + fullSizeMiB + "m|-Xms" + fullSizeMiB + "m|-Xmn" + youngSizeMiB + "m";
		
		RemoteNodeProps.at(node).setRemoteJavaExec(jvm);
		JvmProps.at(node).addJvmArg(memCmd);
		
		String algoCmd;
		String algo = dataPoint.get(GC_ALGO);
		if (dataPoint.get(GC_THREADS) != null) {
			int threads = dataPoint.getInteger(GC_THREADS);
			
			if (GC_ALGO__CMS.equals(algo)) {
				if (threads == 0) {
					algoCmd = "|-XX:-UseParNewGC|-XX:+UseConcMarkSweepGC";
				}
				else {
					algoCmd = "|-XX:+UseParNewGC|-XX:+UseConcMarkSweepGC";
				}
			}
			else if (GC_ALGO__PMSC.equals(algo)) {
				if (threads == 0) {
					algoCmd = "-XX:+UseSerialGC";
				}
				else {
					algoCmd = "-XX:+UseParallelOlgGC";
				}			
			}
			else if (GC_ALGO__G1.equals(algo)) {
				algoCmd = "-XX:+UseG1GC";
			}
			else {
				throw new IllegalArgumentException("Unsupported algo: " + algo);
			}

			if (threads > 0) {
				JvmProps.at(node).addJvmArg("-XX:ParallelGCThreads=" + threads);
			}

			JvmProps.at(node).addJvmArg(algoCmd);
		}
		else {
			if (GC_ALGO__CMS.equals(algo)) {
				algoCmd = "-XX:+UseConcMarkSweepGC";
			}
			else if (GC_ALGO__PMSC.equals(algo)) {
				algoCmd = "-XX:+UseParallelOlgGC";
			}
			else if (GC_ALGO__G1.equals(algo)) {
				algoCmd = "-XX:+UseG1GC";
			}
			else {
				throw new IllegalArgumentException("Unsupported algo: " + algo);
			}

			JvmProps.at(node).addJvmArg(algoCmd);			
		}
		
		
		
		
		if ("true".equalsIgnoreCase(dataPoint.get(COOPS))) {
			JvmProps.at(node).addJvmArg("-XX:+UseCompressedOops");
		}
		else {
			JvmProps.at(node).addJvmArg("-XX:-UseCompressedOops");
		}
		
		if ("true".equalsIgnoreCase(dataPoint.get(DRYMODE))) {
			JvmProps.at(node).addJvmArg("-Ddrymode=true");
		}
		
		String strides = dataPoint.get(GC_STRIDES);
		if (strides != null && !"default".equals(strides)) {
			JvmProps.at(node).addJvmArg("|-XX:+UnlockDiagnosticVMOptions|-XX:ParGCCardsPerStrideChunk=" + strides);
		}
	}
}
