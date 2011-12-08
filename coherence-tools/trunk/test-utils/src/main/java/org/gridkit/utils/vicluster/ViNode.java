/**
 * Copyright 2011 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.utils.vicluster;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;

import org.gridkit.coherence.util.classloader.Isolate;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ViNode implements ViProps {

	@SuppressWarnings("unused")
	private ViCluster cluster;
	
	private Isolate isolate;
	private boolean started;
	private boolean terminated;
	
	public ViNode(ViCluster cluster, String name, Isolate isolate) {
		this.cluster = cluster;
		this.isolate = isolate;
	}

	public String getName() {
		return isolate.getName();
	}
	
	public Isolate getIsolate() {
		return isolate;
	}
	
	public void addToClasspath(URL path) {
		isolate.addToClasspath(path);
	}
	
	public void removeFromClasspath(URL path) {
		isolate.removeFromClasspath(path);
	}

	@Override
	public void setProp(String prop, String value) {
		isolate.setProp(prop, value);
	}

	public void setProp(Map<String, String> props) {
		isolate.setProp(props);
	}

	public void start() {
		if (!started) {
			isolate.start();
			started = true;
		}
	}
	
	public void start(final Class<?> main, final String... args) {
		if (!started) {
			start();
		}
		final String name = getName();
		isolate.exec(new Runnable() {
			@Override
			public void run() {
				Thread t = new ViMain(args, main);
				t.setDaemon(true);
				t.setName(name + "-Main");
				t.start();
			}
		});
	}

	public void exec(Runnable task) {
		if (!started) {
			start();
		}		
		isolate.exec(task);
	}

	public <V> V exec(Callable<V> task) {
		if (!started) {
			start();
		}		
		return isolate.exec(task);
	}

	public <V> V export(Callable<V> task) {
		if (!started) {
			start();
		}		
		return isolate.export(task);
	}

	public void suspend() {
		isolate.suspend();
	}

	public void resume() {
		isolate.resume();
	}

	public Cluster getCluster() {
		if (!started) {
			start();
		}		
		return isolate.export(new Callable<Cluster>() {

			@Override
			public Cluster call() throws Exception {
				return CacheFactory.getCluster();
			}
			
		});
	}

	public NamedCache getCache(final String name) {
		if (!started) {
			start();
		}		
		return isolate.export(new Callable<NamedCache>() {
			
			@Override
			public NamedCache call() throws Exception {
				return CacheFactory.getCache(name);
			}
			
		});
	}

	public String getServiceNameForCache(final String name) {
		if (!started) {
			start();
		}		
		return isolate.exec(new Callable<String>() {
			
			@Override
			public String call() throws Exception {
				return CacheFactory.getCache(name).getCacheService().getInfo().getServiceName();
			}
			
		});
	}

	public Service getService(final String name) {
		if (!started) {
			start();
		}		
		return isolate.export(new Callable<Service>() {
			
			@Override
			public Service call() throws Exception {
				return CacheFactory.getService(name);
			}
			
		});
	}
	
	public void shutdown() {
		if (!terminated) {
			try {
				isolate.exec(new Runnable() {
					@Override
					public void run() {
						if (CacheFactory.getCluster().isRunning()) {
							CacheFactory.getCluster().shutdown();
						}
					}
				});
			}
			catch(Exception e) {
				//ignore
			}
			kill();
		}
	}

	public void kill() {
		if (!started) {
			throw new IllegalStateException("can't kill not started node " + getName());
		}
		if (!terminated) {
			isolate.stop();
			isolate = null;
			terminated = true;
		}
	}

	private static class ViMain extends Thread {
		private final String[] args;
		private final Class<?> main;
	
		private ViMain(String[] args, Class<?> main) {
			this.args = args;
			this.main = main;
		}
	
		@Override
		public void run() {
			try {
				Method mm = main.getDeclaredMethod("main", String[].class);
				mm.setAccessible(true);
				try {
					mm.invoke(null, ((Object)args));
				}
				catch(InvocationTargetException e) {
					if (e.getCause() instanceof ThreadDeath) {
						//ignore;
					}
					else {
						throw e;
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to execute main() at " + main.getName(), e);
			}
		}
	}
}
