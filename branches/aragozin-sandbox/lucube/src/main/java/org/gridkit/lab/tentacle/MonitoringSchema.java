package org.gridkit.lab.tentacle;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.gridkit.lab.gridbeans.ActionGraph;
import org.gridkit.lab.gridbeans.ActionGraph.Action;
import org.gridkit.lab.gridbeans.ActionGraph.ActionSite;
import org.gridkit.lab.gridbeans.ActionGraph.Bean;
import org.gridkit.lab.gridbeans.ActionTracker;
import org.gridkit.lab.tentacle.Locator.LocationManager;
import org.gridkit.lab.tentacle.Locator.TargetAction;
import org.gridkit.lab.tentacle.ObservationHost.ObservationActivity;
import org.gridkit.util.concurrent.TaskService.Task;

public class MonitoringSchema implements Source<DistributedExperiment>, Observable<Source<DistributedExperiment>, DistributedExperiment> {

	enum Global {
		ROOT,
	}
	
	private static final Method SOURCE_AT;
	private static final Method PROBE_MARK_SAMPLE;
	private static final Method PROBE_MARK_SAMPLER;
	private static final Method PROBE_REPORT_PROBE;

	static {
		try {
			SOURCE_AT = Source.class.getMethod("at", Locator.class);
			PROBE_MARK_SAMPLE = Observable.class.getMethod("mark", Sample.class);
			PROBE_MARK_SAMPLER = Observable.class.getMethod("mark", Sampler.class);
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
	public Observable<Source<DistributedExperiment>, DistributedExperiment> mark(Sampler<? super DistributedExperiment, ?> sampler) {
		root.mark(sampler);
		return this;
	}

	@Override
	public MonitoringSchema report(Probe<?, ? super DistributedExperiment> probe) {
		root.report(probe);
		return this;
	}

	public MonitoringConfig prepare() {
		
		ActionGraph graph = tracker.getGraph();
		Bean rootb = graph.getNamed(Global.ROOT);
		
		TargetConfig roote = new TargetConfig();
		captureConfig(graph, rootb, roote);
		
		return new CompiledConfig(roote); 
	}
	
	@SuppressWarnings("rawtypes")
	private void captureConfig(ActionGraph graph, Bean pointer, TargetConfig entry) {
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
				else if (probe instanceof Sampler) {
					probe = new SamplerAsProbe((Sampler)probe);
				}
				entry.probes.add((Probe) probe);
			}
			else {
				schemeException(site, "Cannot interpret");
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void captureDownStream(ActionGraph graph, Bean pointer, Locator locator, TargetConfig parent) {
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
		DownstreamConfig dse = new DownstreamConfig(locator);
		captureConfig(graph, pointer, dse.targetConfig);
		if (!dse.targetConfig.isEmpty()) {
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
		||	site.allMethodAliases().contains(PROBE_MARK_SAMPLER)
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
	
		public void deploy(DistributedExperiment root);
		
	}
	
	
	static class CompiledConfig implements MonitoringConfig {
		
		private TargetConfig rootConfig;

		public CompiledConfig(TargetConfig root) {
			this.rootConfig = root;
		}		
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("<config>");
			rootConfig.dump(sb);
			sb.append("\n</config>");
			return TenUtils.indent(sb.toString(), 4);
		}

		@Override
		public void deploy(DistributedExperiment root) {
			
			Configurator conf = new Configurator(root);
			conf.apply(rootConfig);
			conf.deploy();
		}
	}
	
	static class StackableConfig implements TargetAction<MonitoringTarget>, Serializable {

		private static final long serialVersionUID = 20130510L;

		private final List<TargetConfig> config;
		
		public StackableConfig(TargetConfig c) {
			config = Collections.singletonList(c);
		}

		public StackableConfig(StackableConfig a, StackableConfig b) {
			config = new ArrayList<TargetConfig>(a.config.size() + b.config.size());
			config.addAll(a.config);
			config.addAll(b.config);
		}
		
		@Override
		public TargetAction<MonitoringTarget> stack(TargetAction<?> other) {
			if (other instanceof StackableConfig) {
				StackableConfig that = (StackableConfig) other;
				return new StackableConfig(this, that);
			}
			throw new IllegalArgumentException("Cannot stack");
		}

		@Override
		public void apply(MonitoringTarget target) {
			Configurator configurator = new Configurator(target);
			
			for(TargetConfig tc: this.config) {
				configurator.apply(tc);
			}
			
			configurator.deploy();
		}
	}
	
	@SuppressWarnings("rawtypes")
	static class Configurator {
		
		private final MonitoringTarget target;

		private List<LocationManager> managers = new ArrayList<LocationManager>();

		public Configurator(MonitoringTarget target) {
			this.target = target;
		}
		
		@SuppressWarnings("unchecked")
		public void apply(TargetConfig config) {
			for(Probe p : config.probes) {
				p.deploy(target);
			}
			downstreams:
			for(DownstreamConfig cd : config.downstream) {
				Locator loc = cd.locator;
				for (LocationManager m: managers) {
					if (loc.isCompatible(m)) {
						m.addAction(loc, new StackableConfig(cd.targetConfig));
						continue downstreams;
					}
				}
				LocationManager m = loc.newLocationManager();
				managers.add(m);
				m.bind(target);
				m.addAction(loc, new StackableConfig(cd.targetConfig));
			}
		}
		
		public void deploy() {
			for(LocationManager m: managers) {
				m.deploy();
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	static class TargetConfig implements Serializable {
		
		private static final long serialVersionUID = 20130510L;
		
		private List<DownstreamConfig> downstream = new ArrayList<DownstreamConfig>();
		private List<Probe> probes = new ArrayList<Probe>();
		
		public boolean isEmpty() {
			if (!probes.isEmpty()) {
				return false;
			}
			for(DownstreamConfig ds: downstream) {
				if (!ds.targetConfig.isEmpty()) {
					return false;
				}
			}
			return true;
		}
		
		public void dump(StringBuilder sb) {
			if (!downstream.isEmpty()) {
				sb.append("\n<subtargets>");
				for(DownstreamConfig ds: downstream) {
					sb.append("\n<subtarget>");
					sb.append("\n<locator>\n");
					sb.append(ds.locator);
					sb.append("\n</locator>");
					sb.append("\n<target>");
					ds.targetConfig.dump(sb);
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
	static class DownstreamConfig implements Serializable {
		
		private static final long serialVersionUID = 20130510L;
		
		private Locator locator;
		private TargetConfig targetConfig;
		
		public DownstreamConfig(Locator locator) {
			this.locator = locator;
			this.targetConfig = new TargetConfig();
		}
		
	}
	
	private static class SampleAsProbe implements Probe<Sample, MonitoringTarget>, Serializable {

		private static final long serialVersionUID = 20130510L;
		
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

		public String toString() {
			return String.valueOf(sample);
		}
	}

	@SuppressWarnings("rawtypes")
	private static class SamplerAsProbe implements Probe<Sample, MonitoringTarget>, Serializable {
		
		private static final long serialVersionUID = 20130510L;
		
		private final Sampler sampler;
		
		public SamplerAsProbe(Sampler sampler) {
			this.sampler = sampler;
		}
		
		@Override
		public void deploy(final MonitoringTarget target) {
			target.getBoundTaskService().schedule(new Task() {
				
				@Override
				@SuppressWarnings("unchecked")
				public void run() {
					Sample sample = sampler.sample(target);
					target.getObservationHost().observer(sample.getClass()).observe(sample);
				}
				
				@Override
				public void interrupt(Thread taskThread) {
					// ignore
				}
				
				@Override
				public void cancled() {
					// ignore
				}
			});
		}
		
		public String toString() {
			return String.valueOf(sampler);
		}
	}
}
