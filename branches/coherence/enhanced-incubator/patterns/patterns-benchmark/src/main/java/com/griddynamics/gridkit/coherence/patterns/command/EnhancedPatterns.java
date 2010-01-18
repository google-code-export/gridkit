package com.griddynamics.gridkit.coherence.patterns.command;

import com.griddynamics.gridkit.coherence.patterns.command.ContextConfigurationScheme.DefaultContextConfigurationScheme;
import com.oracle.coherence.patterns.command.ContextConfiguration;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class EnhancedPatterns {

	private static final String schemeCacheName = "enhanced-cohere-pattern.schemes";
	private static final String contextMappingSchemeName = "enhanced-command-patterm.context-mapping";	
	
	private static DefaultContextSchemeManager SCHEME_MANAGER = new DefaultContextSchemeManager();
	
	public static ContextSchemeManager getSchemeManager() {
		return SCHEME_MANAGER;
	}
	
	
	private static class DefaultContextSchemeManager implements ContextSchemeManager {

		@Override
		public DefaultContextConfigurationScheme newSchemeObject() {
			return new DefaultContextConfigurationScheme();
		}
		
		@Override
		public ContextConfigurationScheme registerScheme(String name, ContextConfigurationScheme scheme) {
			NamedCache cache = CacheFactory.getCache(schemeCacheName);
			cache.lock(name);
			try {
				if (cache.get(name) == null) {
					DefaultContextConfigurationScheme cs = new DefaultContextConfigurationScheme(name, (DefaultContextConfigurationScheme)scheme);
					cache.put(name, cs);
					return cs;
				}
				else {
					// TODO ignore if schemes are equal
					throw new IllegalArgumentException("Scheme is already defined");
				}
			}
			finally {
				cache.unlock(name);
			}
		}		

		@Override
		public ContextConfiguration createContextConfiguration(String schemeName) {
			NamedCache cache = CacheFactory.getCache(schemeCacheName);
			// TODO handle missing scheme gracefully
			return new SchemeBasedContextConfiguration((ContextConfigurationScheme) cache.get(schemeName));
		}		
	}
}
