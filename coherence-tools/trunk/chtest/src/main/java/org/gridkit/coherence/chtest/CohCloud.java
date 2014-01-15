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

import java.util.Collection;

import org.gridkit.nanocloud.Cloud;
import org.gridkit.vicluster.ViManager;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.isolate.Isolate;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;

public interface CohCloud {

	/**
	 * Shutdown all {@link ViNode}s
	 */
	public void shutdown();

	/**
	 * @see {@link ViManager#listNodes(String)}
	 */
	public Collection<CohNode> listNodes(String namePattern);

	/**
	 * Same as <code>node("**")</code>
	 * @return
	 */
	public CohNode all();

	/**
	 * @return set of nodes matching patterns in list
	 */
	public CohNode nodes(String... namePatterns);

	/**
	 * @return set of nodes matching pattern
	 */
	public CohNode node(String namePattern);

	public Cloud getCloud();

	public interface CohNode extends ViNode {

		/**
		 * @param <code>true</code> - node will run in own JVM, <code>false</code> it will run in parent JVM in {@link Isolate}
		 */
		CohNode outOfProcess(boolean oop);

		/**
		 * Set "tangosol.coherence.cacheconfig" system property.
		 */		
		CohNode cacheConfig(String file);
		
		/**
		 * Set "tangosol.coherence.cacheconfig" to "empty-cache-config.xml".
		 * This is useful if config will be generated programatically.
		 */		
		CohNode useEmptyCacheConfig();

		CohNode mapCache(String cachePattern, String schemeName);

		CohNode mapCache(String cachePattern, CacheConfig.CacheScheme scheme);

		CohNode addScheme(CacheConfig.CacheScheme scheme);

		CohNode addScheme(CacheConfig.ServiceScheme scheme);
		
		/**
		 * Set "tangosol.pof.config" system property.
		 */		
		CohNode pofConfig(String file);

		/**
		 * Set "tangosol.pof.enabled" system property.
		 */		
		CohNode pofEnabled(boolean enabled);

		/**
		 * Set "tangosol.coherence.distributed.localstorage" system property.
		 */		
		CohNode localStorage(boolean enabled);
		
		/**
		 * Use present for embedded Coherence TCMP cluster.
		 */
		CohNode presetFastLocalCluster();
		
		/**
		 * Enabled of disable Coherence TCP Ring / IP Monitor facility.
		 */
		CohNode enableTcpRing(boolean enable);

		/**
		 * Set "tangosol.coherence.tcmp.enabled" system property.
		 */
		CohNode enableTCMP(boolean enable);

		CohNode addWkaAddress(String host, int port);

		CohNode setClusterLocalHost(String host);

		CohNode setClusterLocalPort(int port);
		
		CohNode logLevel(int level);
		
		/**
		 * Enable or disable Coherence MBeans.
		 * <br/>
		 * Below are setting for enabled JMX
		 * <br/>
		 * <code>
		 * tangosol.coherence.management: local-only
		 * tangosol.coherence.management.jvm.all: false
		 * tangosol.coherence.management.remote: false
		 * </code>
		 * <br/>
		 * In case of in-proc ({@link Isolate}) node, MBean isolation will also be enabled.
		 */		
		CohNode enableJmx(boolean enable);
		
		/**
		 * Override TCMP node disconnection timeout.
		 */		
		CohNode setTCMPTimeout(long timeoutMs);
		
		/**
		 * Allows to select particular Coherence version, provided that wrapper jar is added to dependencies.
		 * @param version
		 */
		CohNode useCoherenceVersion(String version);
		
		/**
		 * Configure node to automatically invoke {@link CacheFactory#ensureCluster()} on startup.
		 */
		CohNode autoStartCluster();

		/**
		 * Configure node to automatically invoke {@link DefaultCacheServer#startServices()} on startup.
		 * <br/>
		 * This option simulates {@link DefaultCacheServer#main(String[])}.
		 */
		CohNode autoStartServices();
		
		/**
		 * If <code>true</code> {@link CacheFactory#shutdown()} will be invoked on node shutdown. 
		 */
		// TODO graceful shutdown is provoking perm gen leak
		// TODO need to investigate
		CohNode gracefulShutdown(boolean graceful);
		
		/**
		 * Invokes {@link DefaultCacheServer#startServices()}.
		 */
		void startCacheServer();
		
		/**
		 * Call {@link CacheFactory#ensureCluster()} on node.
		 */				
		void ensureCluster();

		/**
		 * Shutdowns Coherence without terminating VM.
		 */
		void shutdownCluster();		
		
		/**
		 * @return proxy of remote {@link NamedCache} instantiated on node 
		 */
		NamedCache getCache(String cacheName);
		
		/**
		 * @return Service name for {@link NamedCache} with given name
		 */						
		String getServiceNameForCache(String cacheName);
		
		/**
		 * @return proxy of remote {@link Service} instantiated on node 
		 */
		Service ensureService(String serviceName);
		
		CohNode shareClass(Class<?> type);

		CohNode shareClass(String className);

		CohNode sharePackage(String packageName);
		
		/**
		 * Dumps
		 */
		void dumpCacheConfig();
	}
}
