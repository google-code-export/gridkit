package org.gridkit.lab.tentacle;

import java.util.ArrayList;
import java.util.List;

import org.gridkit.lab.tentacle.Locator.LocationManager;
import org.gridkit.lab.tentacle.Locator.TargetAction;
import org.gridkit.lab.tentacle.Locator.TargetActivity;
import org.gridkit.util.concurrent.FutureBox;
import org.gridkit.util.concurrent.FutureEx;

public abstract class CommonLocationManager<T extends MonitoringTarget> implements LocationManager<T> {

	protected T target;
	
	protected List<Action> actions = new ArrayList<Action>();
	protected List<Subnode> nodes = new ArrayList<Subnode>();
	
	protected boolean deployed;
	protected boolean started;
	protected volatile boolean terminated;
	protected FutureBox<Void> errorBox = new FutureBox<Void>();
	
	
	@Override
	public void bind(T target) {
		this.target = target;
		bound();
	}
	
	protected void bound() {
		// do nothing
	}
	
	protected synchronized void addSubnode(Subnode node) {
		nodes.add(node);
		if (deployed && !terminated) {
			applyActions(node);
			node.deploy();
		}		
	}

	protected void applyActions(Subnode node) {
		
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
		if (!terminated && !deployed) {
			beforeDeploy();
			deployed = true;
			
			for(Subnode node: nodes) {
				applyActions(node);
				node.deploy();
			}
		}
	}

	@Override
	public synchronized FutureEx<Void> start() {
		beforeStart();
		started = true;
		
		return errorBox;
	}

	protected void beforeStart() {
		// to be overriden
	}

	protected void error(Exception e) {
		terminated = true;
		try {
			errorBox.setError(e);
		}
		catch(IllegalStateException ee) {
			// ignore
		}
	}
	
	@Override
	public void stop() {
		terminated = true;
		synchronized (this) {
			stopNodes();
			errorBox.setData(null);
		}
	}
	
	protected void stopNodes() {
		// TODO Auto-generated method stub
		
	}
	
	protected abstract boolean evaluate(Locator<T, ?, ?> locator, Subnode node);

	protected abstract TargetActivity deploy(Subnode node, TargetAction<?> script);

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
	
	protected interface Subnode {
		
	}
	
	protected class DefaultSubnode implements Subnode {

		private List<TargetActivity> activities = new ArrayList<TargetActivity>();
		
		
		public void deploy(TargetAction<T> action) {
			
			activities.add(e);
			
		}
	}
}
