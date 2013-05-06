package org.gridkit.lab.gridbeans;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class CopyGraph implements ActionGraph {

	private Map<Bean, MappedBean> beanMap = new HashMap<Bean, MappedBean>();
	
	private List<MappedBean> beans = new ArrayList<MappedBean>();
	private List<CopyActionSite> sites = new ArrayList<CopyActionSite>();
	private List<CopyAction> actions = new ArrayList<CopyAction>();
	
	private Map<Object, MappedBean> namedBeans = new HashMap<Object, MappedBean>();
	
	public CopyGraph() {		
	}
	
	public void merge(ActionGraph other) {
		
		Map<Action, CopyAction> mapping = new HashMap<Action, CopyAction>();
		
		for(ActionSite site: other.allSites()) {
			CopyActionSite scopy = new CopyActionSite(site);
			sites.add(scopy);
			for(Action action: other.allCalls(site)) {
				CopyAction acopy = new CopyAction(scopy, action);
				actions.add(acopy);
			}
		}
		
	}
	
	protected MappedBean mapBean(Bean external) {
		MappedBean mp = beanMap.get(external);
		if (mp == null) {
			if (external instanceof LocalBean) {
				mp = new CopyLocalBean((LocalBean) external);
				beanMap.put(external, mp);				
			}
			else {
				mp = new CopyExternalBean((ExternalBean) external);
				beanMap.put(external, mp);				
			}
		}		
		return mp;
	}

	protected MappedBean mapResultBean(Bean external, Action action) {
		CopyLocalBean lb = (CopyLocalBean) mapBean(external);
		lb.origin = action;
		return lb;
	}
	
	private class Bound {
		
		public CopyGraph getHost() {
			return CopyGraph.this;
		}		
	}
	
	private class CopyActionSite extends Bound implements ActionSite {

		private final int seqNo;
		private final Method method;
		private final Set<Method> methodAliases;
		private final StackTraceElement[] stackTrace;
		
		public CopyActionSite(ActionSite site) {
			seqNo = site.getSeqNo();
			method = site.getMethod();
			methodAliases = site.allMethodAliases();
			stackTrace = site.getStackTrace();			
		}
		
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
			return methodAliases;
		}

		@Override
		public StackTraceElement[] getStackTrace() {
			return stackTrace;
		}

		@Override
		public String toString() {
			return PrintHelper.toString(this);
		}
	}

	private class CopyAction extends Bound implements Action {
		
		private final ActionSite site;
		private Bean hostBean;
		private Bean resultBean;
		private Object[] groundParams;
		private Bean[] beanParams;
		
		private List<CopyAction> dependencies = new ArrayList<CopyAction>();
		
		public CopyAction(ActionSite site, Action action) {
			this.site = site;
			this.hostBean = mapBean(action.getHostBean());
			this.resultBean = mapResultBean(action.getResultBean(), this);
			this.groundParams = action.getGroundParams();
			this.beanParams = new Bean[action.getBeanParams().length];
			for(int i = 0; i != beanParams.length; ++i) {
				Bean eb = action.getBeanParams()[i];
				if (eb != null) {
					this.beanParams[i] = mapBean(eb);
				}
			}
		}
		
		@Override
		public ActionSite getSite() {
			return site;
		}

		@Override
		public Bean getHostBean() {
			return hostBean;
		}

		@Override
		public Bean getResultBean() {
			return resultBean;
		}

		@Override
		public Object[] getGroundParams() {
			return groundParams;
		}

		@Override
		public Bean[] getBeanParams() {
			return beanParams;
		}

		@Override
		public String toString() {
			return PrintHelper.toString(this);
		}
	}
	
	private class MappedBean extends Bound implements Bean {
		
		private WeakReference<Bean> sourceRef;
		
		public MappedBean(Bean external) {
			sourceRef = new WeakReference<Bean>(external);
		}
		
	}
	
	private class CopyExternalBean extends MappedBean implements ExternalBean {

		private Object name;
		private String caption;
		
		public CopyExternalBean(ExternalBean bean) {
			super(bean);
			name = bean.getName();
			caption = bean.toString();
		}
		
		@Override
		public Object getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return caption;
		}
	}

	private class CopyLocalBean extends MappedBean implements LocalBean {

		private Action origin;
		private String caption;
		
		public CopyLocalBean(LocalBean bean) {
			super(bean);
			caption = bean.toString();
		}

		@Override
		public Action getOrigin() {
			return origin;
		}
		
		@Override
		public String toString() {
			return caption;
		}
		
	}
}
