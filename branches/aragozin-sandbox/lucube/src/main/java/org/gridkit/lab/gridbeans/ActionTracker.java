package org.gridkit.lab.gridbeans;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.gridkit.lab.gridbeans.ActionGraph.Action;
import org.gridkit.lab.gridbeans.ActionGraph.ActionSite;
import org.gridkit.lab.gridbeans.ActionGraph.Bean;
import org.gridkit.lab.gridbeans.ActionGraph.BeanResolver;
import org.gridkit.lab.gridbeans.ActionGraph.ExternalBean;
import org.gridkit.lab.gridbeans.ActionGraph.LocalBean;
import org.gridkit.lab.gridbeans.ActionGraph.TrackingObserver;

public class ActionTracker {

	private static Object[] NO_GROUNDS = new Object[0];
	private static UniqueBean[] NO_BEANS = new UniqueBean[0];
	
	private Map<Object, UniqueBean> namedBeans = new HashMap<Object, UniqueBean>();
	
	private AtomicInteger actionCounter = new AtomicInteger();	
	private AtomicInteger siteCounter = new AtomicInteger();	
	private AtomicInteger beanCounter = new AtomicInteger();
	
	private Map<Integer, UniqueBean> beanDeref = new HashMap<Integer, UniqueBean>();
	
	private List<CallSite> sites = new ArrayList<CallSite>();
	private List<Call> calls = new ArrayList<Call>();
	private List<UniqueBean> beans = new ArrayList<UniqueBean>();
	
	private List<TrackingObserver> observers = new ArrayList<TrackingObserver>();
	private List<BeanResolver> resolvers = new ArrayList<BeanResolver>();
	
	private ActionGraph adapter = new GraphAdapter();
	
	public ActionTracker() {
		
	}

	/**
	 * Composite graphs may require consolidated 
	 * @param siteCounter
	 */
	public ActionTracker(AtomicInteger siteCounter) {
		this.siteCounter = siteCounter;
	}
	
	public ActionGraph getGraph() {
		return adapter;
	}
	
	public void addObserver(TrackingObserver obs) {
		observers.add(obs);
	}
	
	public void addResolver(BeanResolver resolver) {
		resolvers.add(resolver);
	}
	
	public Object bean2proxy(Bean bean) {
		if (bean instanceof BeanHandle) {
			BeanHandle bh = (BeanHandle)bean;
			if (bh.getHost() == this) {
				return bh.bean.getProxy();
			}
		}
		throw new IllegalArgumentException("Bean " + bean + " does not belong to this graph");
	}
	
	public Bean proxy2bean(Object proxy) {
		UniqueBean ub = resolveInternalBean(proxy);
		return ub == null ? null : ub.handle;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T inject(Object id, Class<T> type) {
		
		UniqueBean bean = namedBeans.get(id);
		if (bean != null) {
			if (bean.runtimeType != type) {
				throw new IllegalArgumentException("Named bean '" + id  + "' type mismatch: " + bean.runtimeType.getName() + " != " + type.getName());
			}
			else {
				return (T) bean.getProxy();
			}
		}
		else {
			if (!type.isInterface()) {
				throw new IllegalArgumentException("Only interfaces could be injected");
			}
			UniqueBean nbean = new UniqueBean();
			nbean.beanId = beanCounter.getAndIncrement();
			
			nbean.refName = id;
			nbean.runtimeType = type;
			nbean.handle = new EBeanHandle(nbean);
			
			beanDeref.put(nbean.beanId, nbean);
			beans.add(nbean);
			
			return (T) nbean.getProxy();
		}
	}
	
	protected UniqueBean newLocalBean(Class<?> type, Call source) {

		UniqueBean nbean = new UniqueBean();
		nbean.beanId = beanCounter.getAndIncrement();
		
		nbean.runtimeType = type;
		nbean.source = source;
		nbean.handle = new LBeanHandle(nbean);
		
		beanDeref.put(nbean.beanId, nbean);
		beans.add(nbean);
		
		return nbean;

	}
	
	protected UniqueBean resolveInternalBean(Object proxy) {
		if (proxy instanceof TrackedBean) {
			TrackedBean tb = (TrackedBean)proxy;
			if (tb.getHost() != this) {
				return null;
			}
			else if (tb.getTargets().length != 1) {
				return null;
			}
			return tb.getTargets()[0];
		}
		else {
			return null;
		}
	}

	protected UniqueBean resolveBean(ProtoProxy proxy) {
		UniqueBean ub = resolveInternalBean(proxy);
		if (ub != null) {
			return ub;
		}
		for(BeanResolver resolver: resolvers) {
			Bean bb = resolver.resolve(adapter, proxy);
			if (bb != null) {
				if (bb instanceof BeanHandle && ((BeanHandle) bb).getHost() == this) {
					return ((BeanHandle)bb).bean;
				}
				else {
					throw new IllegalArgumentException("Bean does not belong to graph");
				}
			}
		}
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
	
	protected CallSite newSite(Method m, Class<?> runtimeClass) {
		int id = siteCounter.getAndIncrement();
		CallSite site = new CallSite();
		site.seqNo = id;
		site.method = m;
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		int n = 0; 
		for(; n != trace.length; ++n) {
			if (trace[n].getMethodName().equals(m.getName())) {
				StackTraceElement[] subtrace = Arrays.copyOfRange(trace, n + 1, trace.length);
				if (subtrace.length > 0) {
					site.trace = subtrace;
					break;
				}
			}
		}
		if (site.trace == null) {
			site.trace = trace;
		}
		site.methodAliases = new HashSet<Method>(collectAliases(runtimeClass, m));
		
		sites.add(site);
		
		return site;
	}
	
	protected UniqueBean newCallAction(UniqueBean target, CallSite site, Method m, Object[] groundParams, UniqueBean[] beanParams) {
		
		Call call = new Call();
		call.actionId = actionCounter.getAndIncrement();
		call.host = target.beanId;
		call.site = site;
		call.groundParams = groundParams;
		call.beanParams = new int[beanParams.length];
		for(int i = 0; i != beanParams.length; ++i) {
			call.beanParams[i] = beanParams[i] == null ? -1 : beanParams[i].beanId;
		}
		
		Class<?> type = resultType(m);
		if (type != null) {
			UniqueBean bean = newLocalBean(type, call);
			call.beanResult = bean.beanId;
		}
		else {
			call.beanResult = -1;
		}
		
		calls.add(call);
		
		return beanDeref.get(call.beanResult);		
	}
	
	private Class<?> resultType(Method m) {
		Class<?> rt = m.getReturnType();
		if (rt.isInterface()) {
			return rt;
		}
		else {
			return null;
		}
	}

	protected void afterAction(CallSite site) {
		for(TrackingObserver obs: observers) {
			obs.afterAction(adapter, site);
		}
	}
	
	private static int[] toIds(UniqueBean[] beans) {
		int[] ids = new int[beans.length];
		for(int i = 0; i != ids.length; ++i) {
			ids[i] = beans[i].beanId;
		}
		Arrays.sort(ids);
		return ids;
	}
	
	
	private static interface Filter<T> {
		
		public boolean eval(T t);
		
	}
	
	private class GraphAdapter implements ActionGraph {

		private Set<Action> searchActions(Filter<Call> f) {
			Set<Action> result = new LinkedHashSet<Action>();
			for(Call call: calls) {
				if (f.eval(call)) {
					result.add(call);
				}
			}
			return result;
		}

		private Set<Bean> searchBeans(Filter<UniqueBean> f) {
			Set<Bean> result = new LinkedHashSet<Bean>();
			for(UniqueBean bean: beans) {
				if (f.eval(bean)) {
					result.add(bean.handle);
				}
			}
			return result;
		}

		private Set<ActionSite> searchSites(Filter<CallSite> f) {
			Set<ActionSite> result = new LinkedHashSet<ActionSite>();
			for(CallSite site: sites) {
				if (f.eval(site)) {
					result.add(site);
				}
			}
			return result;
		}
		
		@Override
		public Set<Action> allActions() {
			return searchActions(new Filter<Call>() {
				@Override
				public boolean eval(Call t) {
					return true;
				}
			});
		}

		@Override
		public Set<Bean> allBeans() {
			return searchBeans(new Filter<UniqueBean>() {
				@Override
				public boolean eval(UniqueBean t) {
					return true;
				}
			});
		}

		@Override
		public Bean getNamed(Object name) {
			UniqueBean ub = namedBeans.get(name);
			return ub == null ? null : ub.handle;
		}

		@Override
		public Set<ActionSite> allSites() {
			return searchSites(new Filter<CallSite>() {
				@Override
				public boolean eval(CallSite t) {
					return true;
				}
			});
		}

		@Override
		public Set<ActionSite> allSites(final Method method) {
			return searchSites(new Filter<CallSite>() {
				@Override
				public boolean eval(CallSite t) {
					return t.methodAliases.contains(method);
				}
			});
		}

		@Override
		public Set<Action> allCalls(final Bean bean, final Method method) {
			return searchActions(new Filter<Call>() {
				@Override
				public boolean eval(Call call) {
					if (bean != null && bean != call.getHostBean()) {
						return false;
					}
					if (method != null && !call.site.methodAliases.contains(method)) {
						return false;
					}
					return true;
				}
			});
		}

		@Override
		public Set<Action> allConsumer(final Bean bean) {
			return searchActions(new Filter<Call>() {
				@Override
				public boolean eval(Call call) {
					return Arrays.asList(call.getBeanParams()).contains(bean);
				}
			});
		}

		private Call verify(Action a) {
			if (a instanceof Call && ((Call) a).getHost() == ActionTracker.this) {
				return (Call) a;
			}
			else {
				throw new IllegalArgumentException("Action '" + a + "' does not belong to this graph");
			}
		}
		
		@Override
		public void addDependency(Action from, Action to) {
			Call a = verify(from);
			Call b = verify(to);
			if (!a.dependencies.contains(b)) {
				a.dependencies.add(b);
			}
		}

		@Override
		public void removeDependency(Action from, Action to) {
			Call a = verify(from);
			Call b = verify(to);
			a.dependencies.remove(b);
		}

		@Override
		public Set<Action> getInitialActions() {
			return searchActions(new Filter<Call>() {
				@Override
				public boolean eval(Call t) {
					return t.dependencies.isEmpty();
				}
			});
		}

		@Override
		public Set<Action> getTerminalActions() {
			Set<Action> result = new LinkedHashSet<Action>();
			for(Call call: calls) {
				result.removeAll(call.dependencies);
			}
			return result;
		}

		@Override
		public Set<Action> getUpstream(Action action) {
			final Call a = verify(action);
			Set<Action> result = new LinkedHashSet<Action>();
			result.addAll(a.dependencies);
			return result;
		}

		@Override
		public Set<Action> getDownstream(Action action) {
			final Call a = verify(action);
			return searchActions(new Filter<Call>() {

				@Override
				public boolean eval(Call t) {
					return t.dependencies.contains(a);
				}
			});
		}

	}
	
	private class BeanHandle implements Bean {
		
		protected UniqueBean bean;
		
		public ActionTracker getHost() {
			return ActionTracker.this;
		}
	}
	
	private class EBeanHandle extends BeanHandle implements ExternalBean {
		
		private UniqueBean bean;

		public EBeanHandle(UniqueBean bean) {
			this.bean = bean;
		}

		@Override
		public Object getName() {
			return bean.refName;
		}
		
		public String toString() {
			return "${" + getName() + "}";
		}
	}

	private class LBeanHandle extends BeanHandle implements LocalBean {
		
		private UniqueBean bean;

		public LBeanHandle(UniqueBean bean) {
			this.bean = bean;
		}

		@Override
		public Action getOrigin() {
			return bean.source;
		}
		
		public String toString() {
			return "${#" + bean.beanId + "/" + bean.runtimeType.getSimpleName() + "}";
		}		
	}
	
	private class UniqueBean {
		
		private int beanId; 

		private Object refName;
		private Call source;

		private Class<?> runtimeType;
		
		private ActionGraph.Bean handle;
		
		private TrackedBean mock;
		
		public TrackedBean getProxy() {
			if (mock == null) {
				BeanInvocationHandler handler = new BeanInvocationHandler(ActionTracker.this, new int[]{beanId}, Collections.<Class<?>>singleton(runtimeType));
				mock = handler.newProxy();
			}
			return mock;
		}
	}
	
	private class Call implements Action {
		
		@SuppressWarnings("unused")
		private int actionId;
		
		private CallSite site;
		private int host;
		
		private Object[] groundParams;
		private int[] beanParams;
		
		private int beanResult;
		
		private List<Call> dependencies = new ArrayList<Call>();

		@Override
		public ActionSite getSite() {
			return site;
		}

		public ActionTracker getHost() {
			return ActionTracker.this;
		}
		
		@Override
		public Bean getHostBean() {
			return toGraphBean(host);
		}

		@Override
		public Bean getResultBean() {
			return toGraphBean(beanResult);
		}

		private Bean toGraphBean(int ref) {
			UniqueBean ub = beanDeref.get(ref);
			return ub == null ? null : ub.handle;
		}

		@Override
		public Object[] getGroundParams() {
			return groundParams;
		}

		@Override
		public Bean[] getBeanParams() {
			Bean[] beans = new Bean[beanParams.length];
			for(int i = 0; i != beanParams.length; ++i) {
				beans[i] = toGraphBean(beanParams[i]);
			}
			return beans;
		}
		
		public String toString() {
			return PrintHelper.toString(this);
		}
	}
	
	private class CallSite implements ActionSite {
		
		private int seqNo;
		private Method method;
		private Set<Method> methodAliases;
		private StackTraceElement[] trace;
		
		@Override
		public int getSeqNo() {
			return seqNo;
		}
		
		@Override
		public Method getMethod() {
			return method;
		}
		
		@Override
		public Set<Method> allMethodAliases() {
			return Collections.unmodifiableSet(methodAliases);
		}
		
		
		@Override
		public StackTraceElement[] getStackTrace() {
			return trace;
		}
		
		public String toString() {
			return PrintHelper.toString(this);
		}
	}
	
	private static class BeanInvocationHandler implements InvocationHandler, ProtoProxy, TrackedBean {
		
		private final ActionTracker host;
		private final int[] targets;
		private final Set<Class<?>> runtimeType;
		
		public BeanInvocationHandler(ActionTracker host, int[] targets, Set<Class<?>> runtimeType) {
			this.host = host;
			this.targets = targets;
			this.runtimeType = runtimeType;
		}

		public TrackedBean newProxy() {
			Class<?>[] facade = new Class<?>[runtimeType.size() + 1];
			runtimeType.toArray(facade);
			facade[facade.length - 1] = TrackedBean.class;
			return (TrackedBean) Proxy.newProxyInstance(getClass().getClassLoader(), facade, this);
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
				
				CallSite site = host.newSite(method, proxy.getClass());
				Object result = processCall(method, args, site);
				host.afterAction(site);
				return result;
			}
		}

		public Object processCall(Method method, Object[] args, CallSite site) {
			
			Object[] grounds = filterGounds(args);
			
			UniqueBean[] beans = filterBeans(args);
			
			UniqueBean[] ftargets = getTargets();
			
			UniqueBean[] results = new UniqueBean[ftargets.length];
			
			for(int i = 0; i != ftargets.length; ++i) {
				results[i] = host.newCallAction(ftargets[i], site, method, grounds, beans);
			}
			
			if (results[0] == null) {
				return null;
			}
			else {
				
				if (results.length == 1) {
					return results[0].getProxy();
				}
				else {
					ProtoProxy first = results[0].getProxy();
					Object[] rest = new Object[results.length - 1];
					for(int i = 0; i !=rest.length; ++i) {
						rest[i] = results[i + 1].getProxy();
					}
					return first.lump(rest);
				}
			}
		}

		private Object[] filterGounds(Object[] args) {
			if (args == null) {
				return NO_GROUNDS;
			}
			Object[] grounds = new Object[args.length];
			for(int i = 0; i != args.length; ++i) {
				if (!(args[i] instanceof ProtoProxy)) {
					grounds[i] = args[i];
				}
			}
			return grounds;
		}
		
		private UniqueBean[] filterBeans(Object[] args) {
			if (args == null) {
				return NO_BEANS;
			}
			UniqueBean[] beans = new UniqueBean[args.length];
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
		public UniqueBean[] getTargets() {
			List<UniqueBean> uniques = new ArrayList<UniqueBean>();
			for(int i: targets) {
				UniqueBean ub = host.beanDeref.get(i);
				if (!uniques.contains(ub)) {
					uniques.add(ub);
				}
			}
			return uniques.toArray(new UniqueBean[uniques.size()]);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T lump(Object... proxies) {
			if (proxies == null) {
				return (T)this;
			}
			
			Set<UniqueBean> ntargets = new HashSet<UniqueBean>();
			
			ntargets.addAll(Arrays.asList(getTargets()));
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

			Set<Class<?>> types = new HashSet<Class<?>>();
			for(UniqueBean bi: ntargets) {
				types.add(bi.runtimeType);
			}
			
			Set<Class<?>> facade = new HashSet<Class<?>>(collectInterfaces(types.iterator().next()));
			for(Class<?> rt: types) {
				facade.retainAll(collectInterfaces(rt));
			}
			
			if (facade.isEmpty()) {
				throw new IllegalArgumentException("Cannot find common interface for types: " + types);
			}
			
			BeanInvocationHandler handler = new BeanInvocationHandler(host, toIds(ntargets.toArray(new UniqueBean[0])), facade);
			
			return (T) handler.newProxy();
		}
	}

	static List<Class<?>> collectInterfaces(Class<?> type) {		
		List<Class<?>> result = new ArrayList<Class<?>>();
		if (type.isInterface()) {
			result.add(type);
		}
		for(Class<?> i: type.getInterfaces()) {
			result.add(i);
		}
		if (type.getSuperclass() != Object.class) {
			for(Class<?> i: collectInterfaces(type.getSuperclass())) {
				if (!result.contains(i)) {
					result.add(i);
				}
			}
		}
		return result;
	}
	
	static List<Method> collectAliases(Class<?> targetType, Method m) {
		List<Method> result = new ArrayList<Method>();
		for(Class<?> t: collectInterfaces(targetType)) {
			try {
				Method ma = t.getMethod(m.getName(), m.getParameterTypes());
				if (!result.contains(ma)) {
					result.add(ma);
				}
			} catch (NoSuchMethodException e) {
				// ignore
			}
		}
		return result;
	}

	private interface TrackedBean extends ProtoProxy {
		
		public ActionTracker getHost();
		
		public UniqueBean[] getTargets();
		
	}
	
}
