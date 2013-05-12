package org.gridkit.lab.tentacle;

import java.util.Collection;
import java.util.Map;

public interface JvmProcess extends MonitoringTarget {

	public String getSystemProperty(String prop);

	public Map<String, String> getSystemProperties(Collection<String> prop);
	
}
