package org.gridkit.lab.tentacle;

public interface ActiveNode extends MonitoringTarget {

	public static final ActiveNodeExpander ALL = new ActiveNodeExpander();

	public static final Annotator<ActiveNode> HOSTNAME = null;
	
	public static class ActiveNodeExpander implements SourceExpander<Experiment, ActiveNode, ActiveNodeSource> {

		@Override
		public ActiveNodeSource expand(MonitoringSchema schema, Source<Experiment> source) {
			return null;
		}
	}
	
	public interface ActiveNodeSource extends Source<ActiveNode>, Observable<ActiveNodeSource, ActiveNode> {
	
		public ActiveNodeSource filter(String pattern);
		
		
	}
}
