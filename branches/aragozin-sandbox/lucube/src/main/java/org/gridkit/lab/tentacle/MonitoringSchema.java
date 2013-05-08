package org.gridkit.lab.tentacle;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.gridkit.lab.gridbeans.ActionGraph;
import org.gridkit.lab.gridbeans.ActionGraph.Action;
import org.gridkit.lab.gridbeans.ActionGraph.ActionSite;
import org.gridkit.lab.gridbeans.ActionGraph.Bean;
import org.gridkit.lab.gridbeans.ActionTracker;
import org.gridkit.lab.tentacle.ObservationHost.ObservationActivity;

public class MonitoringSchema implements Source<DistributedExperiment>, Observable<Source<DistributedExperiment>, DistributedExperiment> {

	enum Global {
		ROOT,
	}
	
	private static final Method SOURCE_AT;
	private static final Method PROBE_MARK_SAMPLE;
	private static final Method PROBE_REPORT_PROBE;

	static {
		try {
			SOURCE_AT = Source.class.getMethod("at", Locator.class);
			PROBE_MARK_SAMPLE = Observable.class.getMethod("mark", Sample.class);
			PROBE_REPORT_PROBE = Observable.class.getMethod("report", Probe.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private SchemaTracker tracker = new SchemaTracker();
	
	private RootSource root;
	
	public MonitoringSchema() {
		tracker = new SchemaTracker();
		root = tracker.inject(Global.ROOT, RootSource.class);
	}
	
	@Override
	public <E extends MonitoringTarget, S extends Source<E>> S at(Locator<DistributedExperiment, E, S> locator) {
		return root.at(locator);
	}

	@Override
	public <S extends Source<?>> S hosts(Class<S> hostType) {
		return root.hosts(hostType);
	}
	
	@Override
	public MonitoringSchema mark(Sample sample) {
		root.mark(sample);
		return this;
	}

	@Override
	public MonitoringSchema report(Probe<?, DistributedExperiment> probe) {
		root.report(probe);
		return this;
	}

	public MonitoringConfig prepare() {
		
		Map<Bean, TargetEntry> beanMapping = new HashMap<Bean, TargetEntry>();
		
		ActionGraph graph = tracker.getGraph();
		Bean rootb = graph.getNamed(Global.ROOT);
		
		TargetEntry roote = new TargetEntry();
		captureConfig(graph, rootb, roote);
		
		return new CompiledSchema(roote); 
	}
	
	@SuppressWarnings("rawtypes")
	private void captureConfig(ActionGraph graph, Bean pointer, TargetEntry entry) {
		for(Action a : graph.allActions(pointer, null)) {
			ActionSite site = a.getSite();
			if (site.allMethodAliases().contains(SOURCE_AT)) {
				Locator l = (Locator) a.getGroundParams()[0];
				if (l == null) {
					throw new IllegalArgumentException("Locator is null");
				}
				captureDownStream(graph, a.getResultBean(), l, entry);
			}
			else if (isFilterCall(site)) {
				// ignore
			}
			else if (isProbeCall(site)) {
				Object probe = a.getGroundParams()[0];
				if (probe instanceof Sample) {
					probe = new SampleAsProbe((Sample)probe);
				}
				entry.probes.add((Probe) probe);
			}
			else {
				schemeException(site, "Cannot interpret");
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void captureDownStream(ActionGraph graph, Bean pointer, Locator locator, TargetEntry parent) {
		for(Action a : graph.allActions(pointer, null)) {
			ActionSite site = a.getSite();
			if (isFilterCall(site)) {
				// chained filter applied on locator
				Locator down = safeFilterInvoke(locator, a);
				captureDownStream(graph, a.getResultBean(), down, parent);
			}
			else {
				// only filter calls are interested
			}
		}		
		DownstreamEntry dse = new DownstreamEntry(locator);
		captureConfig(graph, pointer, dse.target);
		if (!dse.target.isEmpty()) {
			parent.downstream.add(dse);
		}
	}

	@SuppressWarnings("rawtypes")
	private Locator safeFilterInvoke(Locator base, Action action) {
		ActionSite site = action.getSite();
		Method m = null;
		for(Method ma : site.allMethodAliases()) {
			if (ma.getDeclaringClass().isAssignableFrom(base.getClass())) {
				m = ma;
				break;
			}
		}
		
		if (m == null) {
			schemeException(site, "Cannot invoke method on " + base);
		}
		
		Locator result;
		try {
			result = (Locator) m.invoke(base, action.getGroundParams());
		}
		catch (Exception e) {
			schemeException(site, e.toString());
			throw new Error("Unreachable");
		}
		return result;
	}
	
	private void schemeException(ActionSite site, String message) {
		schemeException(site, message, null);
	}

	private void schemeException(ActionSite site, String message, Throwable exception) {
		if (exception instanceof InvocationTargetException) {
			exception = exception.getCause();
		}
		ExecutionException wrapper = new ExecutionException("Calling '" + site.getMethod().getName() + "'", exception);
		wrapper.setStackTrace(site.getStackTrace());
		
		throw new IllegalArgumentException(message, wrapper);
	}

	public boolean isFilterCall(ActionSite site) {
		return LocationFilterable.class.isAssignableFrom(site.getMethod().getDeclaringClass());
	}

	public boolean isProbeCall(ActionSite site) {
		return 
			site.allMethodAliases().contains(PROBE_MARK_SAMPLE)
		||	site.allMethodAliases().contains(PROBE_REPORT_PROBE);
	}

	ActionGraph getActionGraph() {
		return tracker.getGraph();
	}
	
	/**
	 * @deprecated Used internally
	 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
	 */
	@Deprecated
	public static interface RootSource extends Source<DistributedExperiment>, Observable<Source<DistributedExperiment>, DistributedExperiment> {
		
	}
	
	private class SchemaTracker extends ActionTracker {
	
		
	}
	
	public interface MonitoringConfig {
	
		public void deploy(String localname, MonitoringTarget root);
		
	}
	
	
	static class CompiledSchema implements MonitoringConfig {
		
		private TargetEntry root;

		public CompiledSchema(TargetEntry root) {
			this.root = root;
		}		
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("<schema>");
			root.dump(sb);
			sb.append("\n</schema>");
			return TenUtils.indent(sb.toString(), 4);
		}
	}
	
	static class TargetEntry implements Serializable {
		
		private List<DownstreamEntry> downstream = new ArrayList<DownstreamEntry>();
		private List<Probe> probes = new ArrayList<Probe>();
		
		public boolean isEmpty() {
			if (!probes.isEmpty()) {
				return false;
			}
			for(DownstreamEntry ds: downstream) {
				if (!ds.target.isEmpty()) {
					return false;
				}
			}
			return true;
		}
		
		
		public void dump(StringBuilder sb) {
			if (!downstream.isEmpty()) {
				sb.append("\n<subtargets>");
				for(DownstreamEntry ds: downstream) {
					sb.append("\n<subtarget>");
					sb.append("\n<locator>\n");
					sb.append(ds.locator);
					sb.append("\n</locator>");
					sb.append("\n<target>");
					ds.target.dump(sb);
					sb.append("\n</target>");
					sb.append("\n</subtarget>");
				}
				sb.append("\n</subtargets>");
			}
			if (!probes.isEmpty()) {
				sb.append("\n<probes>");
				for(Probe p: probes) {
					sb.append("\n<probe>\n");
					sb.append(p);
					sb.append("\n</probe>");
				}
				sb.append("\n</probes>");
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	static class DownstreamEntry implements Serializable {
		
		private Locator locator;
		private TargetEntry target;
		
		public DownstreamEntry(Locator locator) {
			this.locator = locator;
			this.target = new TargetEntry();
		}
		
	}
	
	public interface SingleNodeEnvironment {
		
		public SampleSink getRootSink();
		
		public String nodename();
		
	}
	
	private static class SampleAsProbe implements Probe<Sample, MonitoringTarget>, Serializable {

		private final Sample sample;
		
		public SampleAsProbe(Sample sample) {
			this.sample = sample;
		}

		@Override
		public void deploy(MonitoringTarget target) {
			final Observer<Sample> obs = target.getObservationHost().observer(sample.getClass());
			target.getObservationHost().addActivity(new ObservationActivity() {
				
				@Override
				public void start() {
					obs.observe(sample);
				}
				
				@Override
				public void stop() {
				}
			});
		}

		@Override
		public void deploy(MonitoringTarget target, double rate) {
			throw new UnsupportedOperationException();
		}
		
		public String toString() {
			return String.valueOf(sample);
		}
	}
}
