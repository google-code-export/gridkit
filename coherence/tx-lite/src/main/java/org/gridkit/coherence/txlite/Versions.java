package org.gridkit.coherence.txlite;

class Versions {
	
	public static int LATEST_VERSION = Integer.MAX_VALUE;
	public static int BASELINE_VERSION = 0;
	
	public static int inc(int version) {
		// TODO version recycling
		return version + 1;
	}

	public static boolean greater(int v1, int v2) {
		// TODO version overflow
		return v1 > v2;
	}

}
