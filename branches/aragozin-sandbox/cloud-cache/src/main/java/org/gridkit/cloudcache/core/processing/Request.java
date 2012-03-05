package org.gridkit.cloudcache.core.processing;

import org.gridkit.cloudcache.core.data.Opaque;

public interface Request {

	public Opaque<?> getKey();
	
}
