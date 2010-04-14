/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
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
package com.griddynamics.convergence.demo.dar.service;

import java.io.Serializable;
import java.util.Arrays;

public interface DataAccessStrategy {

	public String route(String dataHost);

	public static class Neutral implements DataAccessStrategy, Serializable {

		private static final long serialVersionUID = 20090420L;
		
		public String route(String dataHost) {
			return null;
		}
		
		@Override
		public String toString() {
			return "Neutral";
		}
	}

	public static class Aware implements DataAccessStrategy, Serializable {

		private static final long serialVersionUID = 20090420L;
		
		public String route(String dataHost) {
			return dataHost;
		}
		
		@Override
		public String toString() {
			return "Data aware";
		}
	}

	public static class AntiAware implements DataAccessStrategy, Serializable {
		
		private static final long serialVersionUID = 20090420L;
		
		private String[] servers;

		public AntiAware(String[] servers) {
			this.servers = servers;
		}

		public String route(String dataHost) {
			if (dataHost != null) {
				dataHost = servers[(Arrays.asList(servers).indexOf(dataHost) + 1) % servers.length];
			}
			
			return dataHost;
		}

		@Override
		public String toString() {
			return "Anti data aware";
		}
	}
	
}
