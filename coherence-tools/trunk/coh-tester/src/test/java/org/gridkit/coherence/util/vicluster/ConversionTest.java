package org.gridkit.coherence.util.vicluster;

import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.gridkit.util.coherence.cohtester.ClusterFactory;
import org.gridkit.vicluster.ViManager;
import org.junit.Test;

public class ConversionTest {

	@Test
	public void test_annonimous_primitive_in_args() {
		
		ViManager c = ClusterFactory.createEmbededTestCluster();
		
		final boolean fb = trueConst();
		final int fi = int_10();
		final double fd = double_10_1();
		
		c.node("node").exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				Assert.assertEquals("fb", true, fb);
				Assert.assertEquals("fi", 10, fi);
				Assert.assertEquals("fd", 10.1d, fd);
				return null;
			}			
		});
		
		c.shutdown();
	}

	private double double_10_1() {
		return 10.1d;
	}

	private int int_10() {
		return 9 + 1;
	}

	private boolean trueConst() {
		return true & true;
	}	
}
