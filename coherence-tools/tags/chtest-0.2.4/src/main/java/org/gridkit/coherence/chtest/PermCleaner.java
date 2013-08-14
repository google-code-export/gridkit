package org.gridkit.coherence.chtest;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermCleaner {

	public static MemoryPoolMXBean PERM_SPACE_MBEAN = getPermSpaceName();
	
	static MemoryPoolMXBean getPermSpaceName() {
		for(MemoryPoolMXBean mb: ManagementFactory.getMemoryPoolMXBeans()) {
			if (mb.getType() == MemoryType.NON_HEAP && mb.getName().toLowerCase().contains("perm") && !mb.getName().toLowerCase().contains("shared")) {
				return mb;
			}
		}
		return null;
	}
	
	public static long getPermSpaceUsage() {
		return PERM_SPACE_MBEAN == null ? 0 : PERM_SPACE_MBEAN.getUsage().getUsed();
	}

	public static long getPermSpaceLimit() {
		return PERM_SPACE_MBEAN == null ? 0 : PERM_SPACE_MBEAN.getUsage().getMax();
	}
	
	public static void forcePermSpaceGC(double factor) {
		if (PERM_SPACE_MBEAN == null) {
			// probably not a HotSpot JVM
			return;
		}
		else {
			double f = ((double)getPermSpaceUsage()) / getPermSpaceLimit();
			if (f > factor) {
				
				List<String> bloat = new ArrayList<String>();
				int spree = 0;
				int n = 0;
				while(spree < 5) {
					try {
						byte[] b = new byte[1 << 20];
						Arrays.fill(b, (byte)('A' + ++n));
						bloat.add(new String(b).intern());
						spree = 0;
					}
					catch(OutOfMemoryError e) {
						++spree;
						System.gc();
					}
				}
				return;
			}
		}
	}	
}
