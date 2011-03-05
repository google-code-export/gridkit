package com.medx.framework.proxy.serialization;

import com.medx.framework.proxy.MapProxy;

public interface MapProxyBinarySerializer extends MapProxySerializer<byte[]> {
	byte[] serialize(MapProxy mapProxy);
	
	MapProxy deserialize(byte[] data);
}
