package org.gridkit.coherence.chtest;

import junit.framework.Assert;

import org.junit.Test;

public class JarManagerCheck {

	@Test
	public void list_versions() {
		System.out.println(JarManager.getAllAvailableVersions());
	}

	@Test
	public void find_jar() {
		System.out.println(JarManager.getCoherenceJarPath());
	}
	
	@Test
	public void extract_jar() {
		String path1 = JarManager.getJarPath("3.7.1.1");
		String path7 = JarManager.getJarPath("3.7.1.7");
		String path11 = JarManager.getJarPath("3.7.1.1");
		System.out.println(path1);
		System.out.println(path7);
		System.out.println(path11);
		Assert.assertEquals(path1, path11);
	}
	
	@Test
	public void try_mixed_cluster() {
		SimpleCohCloud cloud = new SimpleCohCloud();
		try {
			
			cloud.all().presetFastLocalCluster();
			cloud.all().outOfProcess(true);
			cloud.node("3.7.1.1").useCoherenceVersion("3.7.1.1");
			cloud.node("3.7.1.5").useCoherenceVersion("3.7.1.5");
			cloud.node("3.7.1.7").useCoherenceVersion("3.7.1.7");
			cloud.all().ensureCluster();
			
		}
		finally {
			cloud.shutdown();
		}
	}
}
