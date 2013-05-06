package org.gridkit.lab.tentacle;

public interface Observable<S extends Source<M>, M extends MonitoringTarget> {

	public <X extends Enum<?>> Observable<S, M> mark(X mark);

	public Observable<S, M> mark(Mark mark);

	public Observable<S, M> mark(String fqn, String value);

	public Observable<S, M> report(Annotator<M> annotator);
	
	public Observable<S, M> sample(Sampler<M> sampler);
	
}
