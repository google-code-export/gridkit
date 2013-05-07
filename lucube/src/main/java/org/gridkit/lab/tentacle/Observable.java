package org.gridkit.lab.tentacle;

public interface Observable<S extends Source<M>, M extends MonitoringTarget> {

	public Observable<S, M> mark(Sample sample);

	public Observable<S, M> mark(Metric<?, M> annotator);

	public Observable<S, M> report(Metric<?, M> annotator, double rate);
	
}
