package org.gridkit.coherence.cachewatchdog;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.JarManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

import com.tangosol.coherence.component.application.console.Coherence;
import com.tangosol.net.DefaultConfigurableCacheFactory;

public class IsolateCluster_with_12_1_2_TestPlan extends IsolateCluster_TestPlan {

	private static String TARGET_VERSION = "12.1.2.0b40177";

	@BeforeClass
	public static void verifyClassPath() {
		try {
			JarManager.getJarPath(TARGET_VERSION).hashCode();
		}
		catch(Exception e) {
			throw new AssumptionViolatedException("Coherence " + TARGET_VERSION + " is not available");
		}
	}
	
	@Before
	public void initCluster() {	
		cloud.all().useCoherenceVersion(TARGET_VERSION);
		cloud.all().setProp("tangosol.coherence.cachefactory", DefaultConfigurableCacheFactory.class.getName());
		super.initCluster();
	}
	
	@Test
	public void verify_version() {
		cloud.node("node").exec(new Runnable() {
			
			@Override
			public void run() {
				Assert.assertEquals("12.1.2.0.0", Coherence.VERSION);
			}
		});
	}
}
