package org.gridkit.lab.tentacle;

public interface LocalJavaProcess extends LocalProcess {

	public static final LocalProcessFinder ALL = new LocalProcessFinder();
	
	
	public static class LocalProcessFinder implements Locator<ActiveNode, LocalJavaProcess, LocalJavaProcessSource> {

		@Override
		public LocalJavaProcessSource expand(MonitoringSchema schema,
				Source<ActiveNode> source) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public interface LocalJavaProcessSource extends Source<LocalJavaProcess>, Observable<LocalJavaProcessSource, LocalJavaProcess> {
	
		public LocalJavaProcessSource filter(String propPattern);
		
		
	}
	
}
