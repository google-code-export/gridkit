package org.gridkit.lab.tentacle;

import org.gridkit.lab.gridbeans.ActionGraph;
import org.gridkit.lab.gridbeans.ActionTracker;

public class MonitoringSchema implements Source<DistributedExperiment> {

	enum Global {
		ROOT,
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
	public Observable<? extends Source<DistributedExperiment>, DistributedExperiment> known() {
		return root.known();
	}

	@Override
	public Observable<? extends Source<DistributedExperiment>, DistributedExperiment> all() {
		return root.all();
	}
	
	public ActionGraph getActionGraph() {
		return tracker.getGraph();
	}
	
	/**
	 * @deprecated Used internally
	 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
	 */
	@Deprecated
	public static interface RootSource extends Source<DistributedExperiment> {
		
	}
	
	private class SchemaTracker extends ActionTracker {
	
		
		
		
	}	
}
