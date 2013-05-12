package org.gridkit.lab.tentacle;

public interface Observable<S extends Source<M>, M extends MonitoringTarget> {

	public Observable<S, M> mark(Sample sample);

	public Observable<S, M> mark(Sampler<? super M, ?> sampler);

	public Observable<S, M> report(Probe<?, ? super M> annotator);
	
}
