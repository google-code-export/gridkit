package org.gridkit.lab.gridbeans;

import java.lang.reflect.Method;
import java.util.Set;

public interface ActionGraph {

	public Set<Action> getAllActions();
	
	public Set<Bean> getAllBeans();
	
	public Set<CallAction> getAllCalls(Bean bean, Method method);
	
	public static interface Bean {

		public String describe();
	}

	public static interface CallSite {
		
		public int getCallId();
		
		public StackTraceElement[] getStackTrace();
		
	}
	
	public static interface Action {

		public CallSite getCallSite();
		
		public String describe();
	}
	
	public static interface InjectedBean {
		
		public Object getId();
		
	}
	
	public static interface LocalBean {
		
		public CallAction getOrigin();
		
	}
	
	public static interface CallAction extends Action {
		
		public Method getMethod();
		
		public Set<Method> getMethodAliases();
		
		public Bean getResultBean();
		
		public Object[] getGroundParams();
		
		public Bean[] getBeanParams();
		
	}

	public static interface SyncPoint extends Action {
		
	}
}
