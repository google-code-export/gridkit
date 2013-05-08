package org.gridkit.lab.tentacle;

import java.util.concurrent.TimeUnit;

public class Metrics {

	
	private static double S = TimeUnit.SECONDS.toNanos(1);
	private static double N = TimeUnit.MILLISECONDS.toNanos(1);
	
	private static long NANO_ANCHOR = System.nanoTime();
	private static double MILLIS_ANCHOR = N * System.currentTimeMillis();
	
	private static double normalize(long nanotime) {
		return (MILLIS_ANCHOR + (nanotime - NANO_ANCHOR)) / S;
	}
	
	public static double wallclockTime() {
		return normalize(System.nanoTime());
	}
	
	public interface Hostname extends Sample {
		
		public String hostname();
		
	}
	
	public interface Timestamp extends Sample {
		
		public double timestamp();
	}
	
	public interface Alert extends Timestamp {
		
		public String message();
		
		public Throwable expection();
		
	}
	
	public static Alert alert(final String message, final Throwable e) {
		return new Alert() {
			
			@Override
			public double timestamp() {
				return wallclockTime();
			}
			
			@Override
			public String message() {
				return message;
			}
			
			@Override
			public Throwable expection() {
				return e;
			}
		};
	}
}
