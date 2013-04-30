package org.gridkit.lab.tentacle;

public interface SamplerHost<S extends Source<M>, M extends MonitoringTarget> {

	public <X extends Enum<?>> SamplerHost<S, M> mark(X mark);

	public SamplerHost<S, M> mark(Mark mark);

	public SamplerHost<S, M> mark(String fqn, String value);

	public SamplerHost<S, M> report(Annotator<M> annotator);
	
	public SamplerHost<S, M> sample(Sampler<M> sampler);
	
}
