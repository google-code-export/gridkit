package org.gridkit.lab.tentacle;

import org.gridkit.lab.tentacle.Locator.TargetAction;

class ActiveLocationManager extends CommonLocationManager<DistributedExperiment> {

	@Override
	protected void bound() {
		for(String nodename: getTarget().getAllNodes()) {
			addSubnode(new ActiveNodeLocation(nodename));
		}
	}
	
	private class ActiveNodeLocation implements Subnode<DistributedExperiment> {

		private final String nodename;
		
		public ActiveNodeLocation(String nodename) {
			this.nodename = nodename;
		}

		@Override
		public boolean evaluate(Locator<DistributedExperiment, ?, ?> locator) {
			if (locator instanceof ActiveNodeLocator) {
				ActiveNodeLocator al = (ActiveNodeLocator)locator;
				return al.evaluate(nodename);
			}
			else {
				return false;
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void deploy(TargetAction<?> script) {
			getTarget().sendToRemoteNode(nodename, (TargetAction<ActiveNode>)script);			
		}
	}
}