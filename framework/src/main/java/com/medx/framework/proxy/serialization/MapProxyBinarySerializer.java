package com.medx.framework.proxy.serialization;

import com.medx.framework.proxy.MapProxy;

public interface MapProxyBinarySerializer {
	byte[] serialize(MapProxy mapProxy);
	
	MapProxy deserialize(byte[] data);
}
