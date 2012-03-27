package dbg;

import java.lang.management.ManagementFactory;


public class Debug {

	private static final String A = "A";

	public synchronized static void print0() {
		long rt = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
		System.out.println("[" + Thread.currentThread().getName() + "] " + rt + " print0: <>");
		int n = 0;
		for(StackTraceElement e: new Exception().getStackTrace()) {
			System.out.println("  at " + e.toString());
			if (++n > 5) {
				break;
			}
		}
	}

	public synchronized static void print1(Object obj) {
		long rt = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
		System.out.println("[" + Thread.currentThread().getName() + "] " + rt + " print1: " + obj);
		int n = 0;
		for(StackTraceElement e: new Exception().getStackTrace()) {
			System.out.println("  at " + e.toString());
			if (++n > 5) {
				break;
			}
		}
	}
	
	protected void call() {
		print1(A);
	}
	
}
