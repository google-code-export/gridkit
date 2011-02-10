package org.apteka.insurance.service.cache;

import java.util.concurrent.ConcurrentMap;

public interface CacheService {
	ConcurrentMap<Object, Object> getCache(String name);
}
