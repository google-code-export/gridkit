/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
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

package org.gridkit.coherence.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tangosol.net.CacheFactory;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class SchemeSimpleContextTest extends BaseSimpleContextTest {

	@BeforeClass
	public static void init() {
		System.setProperty("tangosol.coherence.wka", "localhost");
		context = new ClassPathXmlApplicationContext("schema/simple-coherence-context.xml");
	}
	
	@AfterClass
	public static void shutdown() {
		context = null;
		CacheFactory.getCluster().shutdown();
	}
	
}
