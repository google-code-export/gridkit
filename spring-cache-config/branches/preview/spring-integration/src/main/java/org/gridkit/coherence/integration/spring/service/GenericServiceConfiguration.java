package org.gridkit.coherence.integration.spring.service;

import com.tangosol.io.Serializer;

public abstract class GenericServiceConfiguration extends AbstractServiceConfiguration {
	
	@ReflectionInjectedProperty("__m_Serializer")
	protected Serializer serializer;
	
	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
}
