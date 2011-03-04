package com.medx.framework.proxy.serialization;

import com.medx.framework.proxy.MapProxy;

public interface MapProxyXmlSerializer {
	String serialize(MapProxy mapProxy);
	
	MapProxy deserialize(String xml);
}
