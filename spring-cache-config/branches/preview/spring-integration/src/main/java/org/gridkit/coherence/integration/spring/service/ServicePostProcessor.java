package org.gridkit.coherence.integration.spring.service;

import com.tangosol.net.Service;

interface ServicePostProcessor {	
	public void postConfigure(Service service);
}
