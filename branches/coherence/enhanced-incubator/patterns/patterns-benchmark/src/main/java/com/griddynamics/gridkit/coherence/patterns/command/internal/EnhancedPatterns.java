package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.util.Map;

import com.griddynamics.gridkit.coherence.patterns.command.ContextConfigurationScheme;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

class EnhancedPatternsHelper {
	
	public static Map<String, ContextConfigurationScheme> getSchemes() {
		return CacheFactory.getCache("enhanced-command-pattern.schemes");
	}

	public static Map<String, String> getContextMapping() {
		return CacheFactory.getCache("enhanced-command-patterm.context-mapping");
	}
	
	public static IdGenerator<Long> getCommandIdGenerator() {
		NamedCache cache = CacheFactory.getCache("enhanced-command-pattern.schemes");
		return CacheIdGeneratorFactory.createLongGenerator(cache, null);
	}
	
}
