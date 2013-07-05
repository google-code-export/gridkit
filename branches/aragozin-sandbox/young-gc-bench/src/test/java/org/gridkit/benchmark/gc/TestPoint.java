package org.gridkit.benchmark.gc;

import java.io.Serializable;

import org.gridkit.benchmark.gc.RemoteBenchRunner.GcMode;
import org.gridkit.vicluster.ViConfigurable;
import org.gridkit.vicluster.telecontrol.jvm.JvmProps;

@SuppressWarnings("serial")
	public class TestPoint implements Serializable, Cloneable {
		
		String javaId;
		GcMode mode;
		int parallelThreads;
		int strides;
		boolean compressedOops;
		boolean largePages;

		int oldSize;
		
		YoungGCPauseBenchmark.TestResult result;
		
		public void configure(ViConfigurable conf, int youngSize) {
			int mx = oldSize + youngSize;
			JvmProps jvmp = JvmProps.at(conf);
			jvmp.addJvmArg("-server");
			jvmp.addJvmArg("|-Xmx" + mx + "m|-Xms" + mx + "m");
			jvmp.addJvmArg("-Xmn" + youngSize  + "m");
			switch(mode) {
			case Serial: 
				jvmp.addJvmArg("-XX:+UseSerialGC");
				break;
			case ParNew: 
				jvmp.addJvmArg("-XX:+UseParNewGC");
				break;
			case CMS_DefNew: 
				jvmp.addJvmArg("|-XX:-UseParNewGC|-XX:+UseConcMarkSweepGC");
				break;
			case CMS_ParNew: 
				jvmp.addJvmArg("|-XX:+UseParNewGC|-XX:+UseConcMarkSweepGC");
				break;
			case G1: 
				jvmp.addJvmArg("-XX:+UseG1GC");
				break;
			}
			if (parallelThreads > 0) {
				jvmp.addJvmArg("-XX:ParallelGCThreads=" + parallelThreads);
			}
			if (strides > 0) {
				jvmp.addJvmArg("-XX:+UnlockDiagnosticVMOptions");
				jvmp.addJvmArg("-XX:ParGCCardsPerStrideChunk=" + strides);
			}
			if (compressedOops) {
				jvmp.addJvmArg("-XX:+UseCompressedOops");
			}
			else {
				jvmp.addJvmArg("-XX:-UseCompressedOops");
			}
//			if (largePages) {
//				jvmp.addJvmArg("-XX:+UseLargePages");
//			}
//			else {
//				jvmp.addJvmArg("-XX:-UseLargePages");
//			}
		}
		
		@Override
		public TestPoint clone() {
			try {
				return (TestPoint)super.clone();
			} catch (CloneNotSupportedException e) {
				throw new Error();
			}
		}



		public String getPointInfo() {
			StringBuilder sb = new StringBuilder();
			sb.append(javaId);
			sb.append(" ").append(mode);
			if (parallelThreads > 0) {
				sb.append(" pthreads:").append(parallelThreads);
			}
			sb.append(" coops:").append(compressedOops ? "on" : "off");
			if (largePages) {
				sb.append(" lpages:").append(largePages ? "on" : "off");
			}
			if (strides > 0) {
				sb.append(" parstride:").append(strides);
			}
			
			return sb.toString();
		}
		
		public int getOldSize() {
			return oldSize;
		}
		
		public double getPauseAvg() {
			return ((double)result.totalTime) / result.youngGcCount;
		}

		public double getPauseStdDev() {
			return Math.sqrt(((double)result.totalSquareTime) / result.youngGcCount - (getPauseAvg() * getPauseAvg()));
		}
	}