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
package org.gridkit.coherence.integration.spring.service;

import org.junit.Ignore;

import com.tangosol.net.PartitionedService;
import com.tangosol.net.partition.KeyAssociator;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@Ignore
public class TestKeyAssociator implements KeyAssociator {

	public TestKeyAssociator() {
		new String();
	}
	
	@Override
	public Object getAssociatedKey(Object paramObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(PartitionedService paramPartitionedService) {
		// TODO Auto-generated method stub
		
	}

}
