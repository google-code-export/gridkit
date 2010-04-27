package com.griddynamics.coherence.integration.spring.config;

/**
 * @author Dmitri Babaev
 */
public enum ServiceType {
	ReplicatedCache,
	OptimisticCache,
	DistributedCache,
	LocalCache,
	Invocation,
	Proxy,
	RemoteCache,
	RemoteInvocation;
}
