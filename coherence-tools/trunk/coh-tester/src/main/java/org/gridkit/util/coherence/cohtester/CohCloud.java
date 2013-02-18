package org.gridkit.util.coherence.cohtester;

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

	public CohNode nodes(String... namePatterns);

	public CohNode node(String namePattern);

	public ViManager getCloud();

	public void useEmbededCluster();

	public void useLocalCluster();

	public interface CohNode extends ViNode {
		
		CohNode cacheConfig(String file);

		CohNode localStorage(boolean enabled);
		
		CohNode enableFastLocalCluster();
		
		/**
		 * Configure node to automatically invoke {@link CacheFactory#ensureCluster()} on startup.
		 */
		CohNode autoStartCluster();

		/**
		 * Configure node to automatically invoke {@link DefaultCacheServer#startServices()} on startup.
		 */
		CohNode autoStartServices();
		
		/**
		 * Invokes {@link DefaultCacheServer#startServices()}.
		 */
		void startCacheServer();
		
		void ensureCluster();
		
		/**
		 * @return proxy of remote {@link NamedCache} instantiated on node 
		 */
		NamedCache getCache(String cacheName);
		
		/**
		 * @return proxy of remote {@link Service} instantiated on node 
		 */
		Service ensureService(String serviceName);
		
	}
}
