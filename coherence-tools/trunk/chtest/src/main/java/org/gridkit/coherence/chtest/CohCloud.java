package org.gridkit.coherence.chtest;

import java.util.Collection;

import org.gridkit.vicluster.ViManager;
import org.gridkit.vicluster.ViNode;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;

public interface CohCloud {

	public void shutdown();

	public Collection<CohNode> listNodes(String namePattern);

	public CohNode all();
	
	public CohNode nodes(String... namePatterns);

	public CohNode node(String namePattern);

	public ViManager getCloud();

	public interface CohNode extends ViNode {

		CohNode outOfProcess(boolean oop);

		CohNode cacheConfig(String file);

		CohNode localStorage(boolean enabled);
		
		CohNode fastLocalClusterPreset();
		
		CohNode enableTcpRing(boolean enable);

		CohNode enableTCMP(boolean enable);
		
		CohNode enableJmx(boolean enable);
		
		CohNode setTCMPTimeout(long timeoutMs);
		
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
		
		void ensureCluster();
		
		/**
		 * @return proxy of remote {@link NamedCache} instantiated on node 
		 */
		NamedCache getCache(String cacheName);
		
		String getServiceNameForCache(String string);

		/**
		 * @return proxy of remote {@link Service} instantiated on node 
		 */
		Service ensureService(String serviceName);
	}
}
