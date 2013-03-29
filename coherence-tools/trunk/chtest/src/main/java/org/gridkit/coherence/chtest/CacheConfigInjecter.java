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

import java.io.Serializable;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.run.xml.XmlElement;

@SuppressWarnings("serial")
class CacheConfigInjecter implements Runnable, Serializable {

	private final XmlConfigFragment fragment;
	
	public CacheConfigInjecter(XmlConfigFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public void run() {
		ConfigurableCacheFactory factory = CacheFactory.getConfigurableCacheFactory();
		XmlElement element = factory.getConfig();
		fragment.inject(element);
		factory.setConfig(element);
	}
}
