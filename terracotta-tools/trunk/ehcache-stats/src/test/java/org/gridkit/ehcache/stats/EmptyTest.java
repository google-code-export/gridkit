/*
 *	
 *	Copyright 2011 Max A. Alexejev.
 *	
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *	
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *	
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an 
 *	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 *	either express or implied. See the License for the specific 
 *	language governing permissions and limitations under the License.
 *	
 */
package org.gridkit.ehcache.stats;

import junit.framework.Assert;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Empty test
 *
 * @author malexejev (malexejev@gmail.com)
 * Sep 18, 2011
 */
public class EmptyTest {
	
	public static final String CACHE_NAME = "test-cache";
	
	private static Ehcache cache;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CacheManager.create();
		cache = CacheManager.getInstance().addCacheIfAbsent(CACHE_NAME);
		Assert.assertNotNull(cache);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		CacheManager.getInstance().shutdown();
	}

	@Before
	public void setUp() throws Exception {
		cache.removeAll();
		cache.clearStatistics();
	}

	@Test
	public void test() {
		Assert.assertNull(cache.get("key"));
	}

}
