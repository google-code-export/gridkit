package org.gridkit.lab.tentacle;



/**
 * {@link ActiveNode} is a highlevel monitoring target with assumption, 
 * what probe code are executed in process being monitoring target.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface ActiveNode extends MonitoringTarget {

	public static final ActiveNodeLocator ALL = new ActiveNodeLocator.AllLocator();
	

	public interface ActiveNodeFilter<X> extends LocationFilterable<X> {
		
		public X filter(String pattern);
		
	}
	
	public interface ActiveNodeSource extends Source<ActiveNode>, Observable<ActiveNodeSource, ActiveNode>, ActiveNodeFilter<ActiveNodeSource> {
	
		public ActiveNodeSource filter(String pattern);
		
	}	
}
