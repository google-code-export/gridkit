package org.gridkit.coherence.chtest;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.junit.Assert;
import org.junit.Test;

public class MavenClassPathTest {

	@Test
	public void verify_self_version() {
		String ver = MavenClasspathManager.getArtifactVersion("org.gridkit.coherence-tools", "chtest");
		System.out.println("verify_self_version: " + ver);
		Assert.assertTrue(ver != null);
	}

	@Test
	public void verify_coherence_version() {
		String ver = MavenClasspathManager.getArtifactVersion("com.oracle.coherence", "coherence");
		System.out.println("verify_coherence_version: " + ver);
		Assert.assertTrue(ver != null);
	}

	@Test
	public void verify_local_repo_detection() {
		File path = MavenClasspathManager.getLocalMavenRepoPath();
		System.out.println("verify_local_repo_detection: " + path);
		Assert.assertTrue(path != null);
	}

	@Test
	public void verify_find_versions() {
		List<String> versions = MavenClasspathManager.getAvailableVersions("com.oracle.coherence", "coherence");
		System.out.println("verify_find_versions: " + versions);
		Assert.assertTrue(!versions.isEmpty());
	}
	
	@Test
	public void verify_find_jar() {
		URL path = MavenClasspathManager.findJar("com.oracle.coherence", "coherence", MavenClasspathManager.getArtifactVersion("com.oracle.coherence", "coherence"));
		URL cppath = MavenClasspathManager.getArtifactClasspathUrl("com.oracle.coherence", "coherence");
		System.out.println("verify_find_jar [jar path]: " + path);
		System.out.println("verify_find_jar [base url]: " + cppath);
		Assert.assertTrue(path != null);
		Assert.assertTrue(cppath != null);
	}
	
	@Test(expected = NoClassDefFoundError.class)
	public void verify_version_replacement__no_class_def() {
		CohCloud cloud = new SimpleCohCloud();
		try {
			CohNode node =cloud.node("chtest-0.2.1");
			MavenClasspathManager.replaceArtifactVersion(node, "org.gridkit.coherence-tools", "chtest", "0.2.1");
			node.exec(new Runnable() {
				
				@Override
				public void run() {
					// this should throw NoClassDefFoundError
					System.out.println("chtest version: " + MavenClasspathManager.getArtifactVersion("org.gridkit.coherence-tools", "chtest"));				
				}
			});
		}
		finally {
			cloud.shutdown();
		}
	}	

	@Test
	public void verify_version_replacement__meta_data() {
		CohCloud cloud = new SimpleCohCloud();
		try {
			CohNode node =cloud.node("chtest-0.2.1");
			MavenClasspathManager.replaceArtifactVersion(node, "org.gridkit.coherence-tools", "chtest", "0.2.1");
			String version = node.exec(new Callable<String>() {
				
				@Override
				public String call() throws Exception {
					// this should throw NoClassDefFoundError
					String cppath = "/META-INF/maven/org.gridkit.coherence-tools/chtest/pom.properties";
					InputStream is = getClass().getResourceAsStream(cppath);
					Properties prop = new Properties();
					prop.load(is);
					is.close();
					return prop.getProperty("version");
				}
			});
			
			System.out.println("chtest version: " + version);
			Assert.assertEquals("0.2.1", version);
		}
		finally {
			cloud.shutdown();
		}
	}	
}
