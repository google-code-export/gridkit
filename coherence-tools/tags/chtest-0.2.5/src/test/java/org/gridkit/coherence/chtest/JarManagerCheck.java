/**
 * Copyright 2013 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.chtest;

import junit.framework.Assert;

import org.junit.Test;

import com.tangosol.coherence.component.application.console.Coherence;

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
		String path352 = JarManager.getJarPath("3.5.2");
		System.out.println(path1);
		System.out.println(path7);
		System.out.println(path11);
		System.out.println(path352);
		Assert.assertEquals(path1, path11);
	}
	
	@Test
	public void try_mixed_cluster_in_proc() {
		try_mixed_cluster(false);
	}

	@Test
	public void try_mixed_cluster_out_of_proc() {
		try_mixed_cluster(true);
	}
	
	public void try_mixed_cluster(boolean outOfProcess) {
		SimpleCohCloud cloud = new SimpleCohCloud();
		try {
			
			cloud.all().presetFastLocalCluster();
			cloud.all().outOfProcess(outOfProcess);
			cloud.node("3.7.1.1").useCoherenceVersion("3.7.1.1");
			cloud.node("3.7.1.3").useCoherenceVersion("3.7.1.3");
			cloud.node("3.7.1.5").useCoherenceVersion("3.7.1.5");
			cloud.node("3.7.1.7").useCoherenceVersion("3.7.1.7");
			cloud.all().ensureCluster();
			
			cloud.node("3.7.1.1").exec(new Runnable() {
				@Override
				public void run() {
					Assert.assertEquals("3.7.1.1", Coherence.VERSION);
				}
			});

			cloud.node("3.7.1.3").exec(new Runnable() {
				@Override
				public void run() {
					Assert.assertEquals("3.7.1.3", Coherence.VERSION);
				}
			});
			
			cloud.node("3.7.1.5").exec(new Runnable() {
				@Override
				public void run() {
					Assert.assertEquals("3.7.1.5", Coherence.VERSION);
				}
			});

			cloud.node("3.7.1.7").exec(new Runnable() {
				@Override
				public void run() {
					Assert.assertEquals("3.7.1.7", Coherence.VERSION);
				}
			});			
		}
		finally {
			cloud.shutdown();
		}
	}
}
