package org.gridkit.lab.tentacle;

import java.util.ArrayList;
import java.util.List;

import org.gridkit.lab.tentacle.Locator.LocationManager;
import org.gridkit.lab.tentacle.Locator.TargetAction;

public class CommonLocationManager<T extends MonitoringTarget> implements LocationManager<T> {

	protected T target;
	
	protected List<Action> actions = new ArrayList<Action>();
	protected List<Subnode<T>> nodes = new ArrayList<Subnode<T>>();
	
	protected boolean deployed;
	protected ObservationHost ohost;

	protected T getTarget() {
		return target;
	}
	
	@Override
	public void bind(T target) {
		this.target = target;
		ohost = target.getObservationHost();
		bound();
	}
	
	protected void error(String message) {
		ohost.reportError(message, null);
	}

	protected void error(Throwable e) {
		ohost.reportError(e.toString(), e);
	}

	protected void error(String message, Throwable e) {
		ohost.reportError(message, e);
	}
	
	protected void bound() {
		// do nothing
	}
	
	protected synchronized void addSubnode(Subnode<T> node) {
		nodes.add(node);
		if (deployed) {
			applyActions(node);
		}		
	}

	protected void applyActions(Subnode<T> node) {
		TargetAction<?> lump = null;
		for(Action a: actions) {
			if (node.evaluate(a.getLocator())) {
				if (lump == null) {
					lump = a.getScript();
				}
				else {
					lump = lump.stack(a.getScript());
				}
			}
		}
		if (lump != null) {
			node.deploy(lump);
		}
	}
	
	@Override
	public synchronized <TT extends MonitoringTarget> void addAction(Locator<T, TT, ? extends Source<TT>> locator, TargetAction<TT> script) {
		if (deployed) {
			throw new IllegalStateException("Cannot add after start");
		}
		Action action = new Action(locator, script);
		actions.add(action);
	}

	protected void beforeDeploy() {
		// to be overriden
	}
	
	@Override
	public synchronized void deploy() {
		beforeDeploy();
		deployed = true;
		
		for(Subnode<T> node: nodes) {
			applyActions(node);
		}
	}


	protected class Action {
		
		final Locator<T, ?, ?> locator;
		final TargetAction<?> script;

		public Action(Locator<T, ?, ?> locator, TargetAction<?> script) {
			this.locator = locator;
			this.script = script;
		}

		public Locator<T, ?, ?> getLocator() {
			return locator;
		}

		public TargetAction<?> getScript() {
			return script;
		}
	}
	
	protected interface Subnode<T extends MonitoringTarget> {

		public boolean evaluate(Locator<T, ?, ?> locator);

		public void deploy(TargetAction<?> script);
		
	}	
}
