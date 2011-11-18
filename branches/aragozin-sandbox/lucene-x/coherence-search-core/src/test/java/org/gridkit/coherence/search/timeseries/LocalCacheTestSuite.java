package org.gridkit.coherence.search.timeseries;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.tangosol.net.CacheFactory;

@RunWith(Suite.class)
@SuiteClasses({
	BasicFunctional.class
})
public class LocalCacheTestSuite {

	@BeforeClass
	public static void init(){
		AbstractTimeseriesFunctionalTest.testCache = CacheFactory.getCache("local-cache");
	}
	
}
