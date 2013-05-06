package org.gridkit.lab.gridbeans;

import static java.util.Collections.replaceAll;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.gridkit.lab.gridbeans.ActionGraph.Bean;

public class ActionTracker {

	private Map<Object, Object> namedBeans = new HashMap<Object, Object>();
	private Map<Object, BeanInfo> mocks = new HashMap<Object, BeanInfo>();
	
	private AtomicInteger actionCounter = new AtomicInteger();	
	private AtomicInteger beanCounter = new AtomicInteger();
	
	public ActionTracker() {
		
	}
	
	public <T> T inject(Object id, Class<T> type) {
		
	}
	
	public ActionGraph getGraph() {
		
	}
	
	protected BeanInfo resolveBean(ProtoProxy proxy) {
		if (proxy instanceof TrackedBean) {
			TrackedBean tb = (TrackedBean)proxy;
			if (tb.getHost() != this) {
				throw new IllegalArgumentException("Bean " + proxy + " belongs to different graph");
			}
			else if (tb.getTargets().length != 1) {
				throw new IllegalArgumentException("Bean " + proxy + " is multi target bean");
			}
			return tb.getTargets()[0];
		}
		else {
			throw new IllegalArgumentException("Bean " + proxy + " belongs to different graph");
		}
		
	}
	
	protected BeanInfo addCallAction(BeanInfo target, int seqNo, Method m, Object[] groundParams, BeanInfo[] beanParams) {
		
	}
	
	private static class BeanInfo {
				
		private ProtoProxy mock;
		
		private Class<?> runtimeTypes;
		
		private ActionGraph.Bean handle;
		
		private Object injectId;
		
		
	}
	
	private static class Invocation {
		
		private int seqNo;
		
		private Method method;
		private Object[] groundParams;
		private BeanInfo[] beanParams;
		
		private BeanInfo result;
		
	}
	
	private static class BeanInvocationHandler implements InvocationHandler, ProtoProxy, TrackedBean {
		
		private final ActionTracker host;
		private final BeanInfo[] targets;
		private final Set<Class<?>> runtimeType;
		
		public BeanInvocationHandler(ActionTracker host, BeanInfo[] targets, Set<Class<?>> runtimeType) {
			this.host = host;
			this.targets = targets;
			this.runtimeType = runtimeType;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getDeclaringClass() == Object.class) {
				return method.invoke(this, args);
			}
			if (method.getDeclaringClass() == ProtoProxy.class) {
				return method.invoke(this, args);
			}
			else if (method.getDeclaringClass() == TrackedBean.class) {
				return method.invoke(this, args);
			}
			else {
				
				int actionId = host.actionCounter.getAndIncrement();
				
				Object[] grounds = filterGounds(grounds, args);
				
				BeanInfo[] beans = filterBeans(beans, args);
				
				BeanInfo[] results = new BeanInfo[targets.length];
				
				for(int i = 0; i != targets.length; ++i) {
					results[i] = host.addCallAction(targets[i], actionId, method, grounds, beans);
				}
				
				if (results[0] == null) {
					return null;
				}
				else {
					
					if (results.length == 1) {
						return results[0].mock;
					}
					else {
						ProtoProxy first = results[0].mock;
						Object[] rest = new Object[results.length - 1];
						for(int i = 0; i !=rest.length; ++i) {
							rest[i] = results[i + 1].mock;
						}
						return first.lump(rest);
					}
				}
			}
		}

		private Object[] filterGounds(Object[] args) {
			Object[] grounds = new Object[args.length];
			for(int i = 0; i != args.length; ++i) {
				if (!(args[i] instanceof ProtoProxy)) {
					grounds[i] = args[i];
				}
			}
			return grounds;
		}
		
		private BeanInfo[] filterBeans(Object[] args) {
			BeanInfo[] beans = new BeanInfo[args.length];
			for(int i = 0; i != args.length; ++i) {
				if (args[i] instanceof ProtoProxy) {
					if (args[i] instanceof TrackedBean && ((TrackedBean)args[i]).getHost() == host && ((TrackedBean)args[i]).getTargets().length == 1) {
						beans[i] = ((TrackedBean)args[i]).getTargets()[0];
					}
					else {
						beans[i] = host.resolveBean((ProtoProxy)args[i]);
					}
				}
			}
			return beans;
		}

		@Override
		public ActionTracker getHost() {
			return host;
		}

		@Override
		public BeanInfo[] getTargets() {
			return targets;
		}

		@Override
		public <T> T lump(Object... proxies) {
			List<BeanInfo> ntargets = new ArrayList<BeanInfo>();
			
			ntargets.addAll(Arrays.asList(targets));
			for(Object p: proxies) {
				if (p instanceof TrackedBean) {
					TrackedBean tb = (TrackedBean) p;
					if (tb.getHost() == host) {
						ntargets.addAll(Arrays.asList(tb.getTargets()));
						continue;
					}
				}
				throw new IllegalArgumentException("Cannot lump with " + p);
			}

			BeanInvocationHandler
			
			return null;
		}
		
		
	}
	

	private interface TrackedBean extends ProtoProxy {
		
		public ActionTracker getHost();
		
		public BeanInfo[] getTargets();
		
	}
	
	public interface TrackingObserver {
		
		public void afterCall(int actionId);
		
	}
}
