/**
 * Copyright 2012 Alexey Ragozin
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
package org.gridkit.vicluster;

import java.util.Map;

/**
 * TODO config calls chaining 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface ViConfigurable {

	public <X> X x(ViExtender<X> extention);
	
	public void setProp(String propName, String value);
	
	public void setProps(Map<String, String> props);
	
	/**
	 * SPI communication method.<br/>
	 * Update runtime configuration element.
	 */
	public void setConfigElement(String key, Object value);

	/**
	 * SPI communication method.<br/>
	 * Update runtime configuration elements.
	 */
	public void setConfigElements(Map<String, Object> config);
	
	public void addStartupHook(String id, Runnable hook);

	public void addShutdownHook(String id, Runnable hook);

	/**
	 * @deprecated Override protection is to be remove 
	 */
	public void addStartupHook(String id, Runnable hook, boolean override);

	/**
	 * @deprecated Override protection is to be remove 
	 */
	public void addShutdownHook(String id, Runnable hook, boolean override);
	
	
	public static abstract class Delegate implements ViConfigurable {
		
		protected abstract ViConfigurable getConfigurable();
		
		public <X> X x(ViExtender<X> extention) {
			return getConfigurable().x(extention);
		}

		public void setProp(String propName, String value) {
			getConfigurable().setProp(propName, value);
		}

		public void setProps(Map<String, String> props) {
			getConfigurable().setProps(props);
		}

		public void setConfigElement(String key, Object value) {
			getConfigurable().setConfigElement(key, value);
		}

		public void setConfigElements(Map<String, Object> config) {
			getConfigurable().setConfigElements(config);
		}

		public void addStartupHook(String id, Runnable hook) {
			getConfigurable().addStartupHook(id, hook);
		}

		public void addShutdownHook(String id, Runnable hook) {
			getConfigurable().addShutdownHook(id, hook);
		}

		public void addStartupHook(String id, Runnable hook, boolean override) {
			getConfigurable().addStartupHook(id, hook, override);
		}

		public void addShutdownHook(String id, Runnable hook, boolean override) {
			getConfigurable().addShutdownHook(id, hook, override);
		}
	}
}
