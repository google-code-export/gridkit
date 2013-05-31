package org.gridkit.coherence.chtest;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class MavenClassPathTest {

	@Test
	public void verify_self_version() {
		String ver = MavenClassPathHelper.getArtifactVersion("org.gridkit.coherence-tools", "chtest");
		Assert.assertTrue(ver != null);
	}

	@Test
	public void verify_coherence_version() {
		String ver = MavenClassPathHelper.getArtifactVersion("com.oracle.coherence", "coherence");
		Assert.assertTrue(ver != null);
	}

	@Test
	public void verify_local_repo_detection() {
		File path = MavenClassPathHelper.getLocalMavenRepoPath();
		Assert.assertTrue(path != null);
	}

	@Test
	public void verify_find_versions() {
		List<String> versions = MavenClassPathHelper.getAvailableVersions("com.oracle.coherence", "coherence");
		Assert.assertTrue(!versions.isEmpty());
	}
	
	@Test
	public void verify_find_jar() {
		URL path = MavenClassPathHelper.findJar("com.oracle.coherence", "coherence", MavenClassPathHelper.getArtifactVersion("com.oracle.coherence", "coherence"));
		URL cppath = MavenClassPathHelper.getArtifactClasspathUrl("com.oracle.coherence", "coherence");
		Assert.assertTrue(path != null);
		Assert.assertTrue(cppath != null);
	}
	
}
