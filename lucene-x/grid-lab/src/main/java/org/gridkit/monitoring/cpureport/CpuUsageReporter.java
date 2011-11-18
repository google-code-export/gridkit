package org.gridkit.monitoring.cpureport;

import java.io.PrintStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.gridkit.util.formating.Formats;

public class CpuUsageReporter {

	public static void startReporter() {
		startReporter(System.out, TimeUnit.SECONDS.toMillis(60));
	}
	
	public static void startReporter(PrintStream ps,long reportInterval) {
		CpuUsageReporterDaemon reporter = new CpuUsageReporterDaemon(ps, reportInterval);
		Thread thread = new Thread(reporter);
		thread.setDaemon(true);
		thread.setName("CPU usage reporter");
		thread.start();
	}
	
	private static class CpuUsageReporterDaemon implements Runnable {
		
		private ThreadMXBean threadBean;
		private List<GarbageCollectorMXBean> gcBeans;
		private PrintStream printStream;
		private long reportInterval;
		private String jvmName = ManagementFactory.getRuntimeMXBean().getName();

		private long lastTimestamp;
		private long lastProcessCpuTime;
		private long lastGcTime;
		private BigInteger lastCummulativeCpuTime;
		private BigInteger lastCummulativeUserTime;
		
		private Map<Long, ThreadNote> notes = new HashMap<Long, CpuUsageReporter.ThreadNote>();
		
		public CpuUsageReporterDaemon(PrintStream ps, long reportInterval) {
			this.printStream = ps;
			this.threadBean =ManagementFactory.getThreadMXBean();
			this.gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
			this.reportInterval = reportInterval;
		}
		
		@Override
		public void run() {
			
			lastTimestamp = System.nanoTime();
			lastProcessCpuTime = getProcessCpuTime();
			lastGcTime = getGcTime();
			
			while(true) {
				
				long currentTime = System.nanoTime();
				long timeSplit = currentTime - lastTimestamp;
				long currentCpuTime = getProcessCpuTime();
				long gcTime = getGcTime();
				
				Map<Long,ThreadNote> newNotes = new HashMap<Long, CpuUsageReporter.ThreadNote>();
				Set<String> report = new TreeSet<String>();
				
				BigInteger totalCpu = BigInteger.valueOf(0);
				BigInteger totalUser = BigInteger.valueOf(0);
				
				
				for(long tid: threadBean.getAllThreadIds()) {
					ThreadNote lastNote = notes.get(tid);
					ThreadNote newNote = new ThreadNote();
					newNote.threadId = tid;
					newNote.lastCpuTime = threadBean.getThreadCpuTime(tid);
					newNote.lastUserTime = threadBean.getThreadUserTime(tid);
					
					newNotes.put(tid, newNote);
					
					totalCpu = totalCpu.add(BigInteger.valueOf(newNote.lastCpuTime));
					totalUser = totalUser.add(BigInteger.valueOf(newNote.lastUserTime));
					
					if (lastNote != null) {

						double cpuT = ((double)(newNote.lastCpuTime - lastNote.lastCpuTime)) / timeSplit;
						double userT = ((double)(newNote.lastUserTime - lastNote.lastUserTime)) / timeSplit;

						StringBuffer buf = new StringBuffer();
						buf.append(String.format("[%06d] user=%.2f%% sys=%.2f%% - %s", tid, 100 * userT, 100 * (cpuT - userT), threadBean.getThreadInfo(tid).getThreadName()));
						report.add(buf.toString());
					}
				}
				
				if (report.size() >0) {				

					double processT = ((double)(currentCpuTime - lastProcessCpuTime)) / timeSplit;
					double cpuT = ((double)(totalCpu.subtract(lastCummulativeCpuTime).longValue())) / timeSplit;
					double userT = ((double)(totalUser.subtract(lastCummulativeUserTime).longValue())) / timeSplit;
					double gcT = ((double)(gcTime - lastGcTime)) / timeSplit;

					StringBuffer buf = new StringBuffer();
					buf.append(Formats.toDatestamp(System.currentTimeMillis()));
					buf.append(String.format(" CPU usage (%s) \n  process cpu=%.2f%%\n  application: cpu=%.2f%% (user=%.2f%% sys=%.2f%%)\n  other: cpu=%.2f%% \n", jvmName, 100 * processT, 100 * cpuT, 100 * userT, 100 * (cpuT - userT), 100 * (processT - cpuT)));
//					buf.append(String.format(" CPU usage (%s) \n  process cpu=%.2f%%\n  application: cpu=%.2f%% (user=%.2f%% sys=%.2f%%)\n  gc: cpu=%.2f%%\n  other: cpu=%.2f%% \n", jvmName, 100 * processT, 100 * cpuT, 100 * userT, 100 * (cpuT - userT), 100 * gcT, 100 * (processT - gcT - cpuT)));
					for(String line: report) {
						buf.append(line).append('\n');
					}
					buf.append("\n");
				
					printStream.append(buf);
				}
				
				lastTimestamp = currentTime;
				notes = newNotes;
				lastCummulativeCpuTime = totalCpu;
				lastCummulativeUserTime = totalUser;
				lastProcessCpuTime = currentCpuTime;
				
				try {
					Thread.sleep(reportInterval);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private long getProcessCpuTime() {
			return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getProcessCpuTime();
		}
		
		private long getGcTime() {
			long cpuTime = 0;
			for(GarbageCollectorMXBean gcBean : gcBeans) {
				cpuTime += TimeUnit.MILLISECONDS.toNanos(gcBean.getCollectionTime());
			}
			return cpuTime;
		}
	}	
	
	private static class ThreadNote {
		
		private long threadId;
		private long lastCpuTime;
		private long lastUserTime;
		
	}
}
