package org.gridkit.cloudcache.core.processing;

import org.gridkit.cloudcache.core.data.Opaque;

public interface Request {
	
	public Object getRequestType();
	
	public boolean isActive();
	
	public <T> void complete(Opaque<T> result);
	
	public void fail(Failure failure);

}
