package org.gridkit.coherence.integration.spring.service;

import com.tangosol.net.Service;
import com.tangosol.run.xml.XmlElement;

public interface ServiceConfiguration {

	public XmlElement getXmlConfiguration();

	public void postConfigure(Service service);

}
