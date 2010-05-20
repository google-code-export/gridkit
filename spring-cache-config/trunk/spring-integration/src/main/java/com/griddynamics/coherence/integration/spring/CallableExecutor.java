package com.griddynamics.coherence.integration.spring;

import java.util.concurrent.Callable;

interface CallableExecutor {
	public <V> V invoke(Callable<V> callable);
}
