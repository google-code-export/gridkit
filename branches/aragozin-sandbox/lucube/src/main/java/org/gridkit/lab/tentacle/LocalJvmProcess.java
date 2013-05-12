package org.gridkit.lab.tentacle;

public interface LocalJvmProcess extends LocalProcess, JvmProcess {

	public static final Locator<ActiveNode, LocalJvmProcess, LocalJvmProcessSource> ALL = new LocalJvmProcessLocator();
	
	public interface LocalJvmProcessFilter {
		
		public boolean evaluate(LocalJvmProcess process);
		
	}
	
	public interface LocalJvmProcessSource extends Source<LocalJvmProcess>, Observable<LocalJvmProcessSource, LocalJvmProcess> {
	
	}
	
}
