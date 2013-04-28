package org.gridkit.coherence.cachewatchdog;

import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.chtest.PermCleaner;
import org.gridkit.coherence.test.CacheTemplate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class IsolateCluster_TestPlan implements PartitionMonitorTestSet {

	@Rule
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	private PartitionMonitorTestSet.Impl partitionMonitorTest;
	
	@Before
	public void forcePermGC() throws InterruptedException {
		System.gc();
		PermCleaner.forcePermSpaceGC(0.5);
		System.gc();
	}
	
	@Before
	public void initCluster() {	
		cloud.all().shareClass(PartitionMonitorTest_Statics.class);
		cloud.all().presetFastLocalCluster();
		CacheTemplate.useTemplateCacheConfig(cloud.all());
		CacheTemplate.usePartitionedInMemoryCache(cloud.all());
		cloud.node("server.*").localStorage(true);
		cloud.node("client.*").localStorage(false);
	}
	
	protected PartitionMonitorTestSet newPartitionMonitorTest() {
		partitionMonitorTest = new PartitionMonitorTestSet.Impl();
		partitionMonitorTest.setCloud(cloud);
		return partitionMonitorTest;
	}

	@Test
	public void verify_vanila_init_case() throws InterruptedException {
		newPartitionMonitorTest().verify_vanila_init_case();
	}

	@Test
	public void verify_fresh_canary_status() {
		newPartitionMonitorTest().verify_fresh_canary_status();
	}

	@Test
	public void verify_parallel_init_case() throws InterruptedException {
		newPartitionMonitorTest().verify_parallel_init_case();
	}

	@Test
	public void verify_parallel_init_case_with_limit() throws InterruptedException {
		newPartitionMonitorTest().verify_parallel_init_case_with_limit();
	}

	@Test
	public void verify_parallel_init_case_with_limit_and_concurency() throws InterruptedException {
		newPartitionMonitorTest().verify_parallel_init_case_with_limit_and_concurency();
	}

	@Test
	public void verify_parallel_init_crash_case() throws InterruptedException {
		PermCleaner.forcePermSpaceGC(0);
		newPartitionMonitorTest().verify_parallel_init_crash_case();
	}

	@Test
	public void verify_simple_recovery_case() throws InterruptedException {
		newPartitionMonitorTest().verify_simple_recovery_case();
	}

	@Test
	public void verify_parallel_recovery_case() throws InterruptedException {
		newPartitionMonitorTest().verify_parallel_recovery_case();
	}
}
