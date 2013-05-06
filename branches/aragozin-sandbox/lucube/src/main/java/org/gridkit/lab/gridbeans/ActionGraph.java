package org.gridkit.lab.gridbeans;

import java.lang.reflect.Method;
import java.util.Set;

public interface ActionGraph {

	public Set<Action> allActions();
	
	public Set<Bean> allBeans();
	
	public Bean getNamed(Object name);
	
	public Set<ActionSite> allSites();

	public Set<ActionSite> allSites(Method method);

	public Set<Action> allCalls(ActionSite site);

	public Set<Action> allCalls(Bean bean, Method method);
	
	public Set<Action> allConsumer(Bean bean);

	public void addDependency(Action from, Action to);

	public void removeDependency(Action from, Action to);

	public Set<Action> getInitialActions();

	public Set<Action> getUpstream(Action action);

	public Set<Action> getDownstream(Action action);

	public Set<Action> getTerminalActions();
	
	public void unify(Bean bean1, Bean bean2);

	public void unify(Action action1, Action action2);
	
	public static interface Bean {

	}

	public static interface ActionSite {
		
		/** Global chronological sequence number */
		public int getSeqNo();
		
		public Method getMethod();

		/**
		 * Runtime method may be not accurate due to nature of proxy class.
		 * This method returns all alternative method declaration.
		 */
		public Set<Method> allMethodAliases();
		
		public StackTraceElement[] getStackTrace();
		
	}
	
	public static interface ExternalBean extends Bean {
		
		public Object getName();
		
	}
	
	public static interface LocalBean extends Bean {
		
		public Action getOrigin();
		
	}
	
	public static interface Action {

		public ActionSite getSite();
		
		public Bean getHostBean();
		
		public Bean getResultBean();
		
		public Object[] getGroundParams();
		
		public Bean[] getBeanParams();
		
	}
	
	public static interface TrackingObserver {
		
		public void afterAction(ActionGraph graph, ActionSite site);
		
	}
	
	public static interface BeanResolver {
		
		public Bean resolve(ActionGraph graph, Object runtimeProxy);
		
	}
}
