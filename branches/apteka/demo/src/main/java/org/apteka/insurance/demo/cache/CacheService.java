package org.apteka.insurance.demo.cache;

import java.util.concurrent.ConcurrentMap;

public interface CacheService {
	ConcurrentMap<Object, Object> getCache(String name);
}
