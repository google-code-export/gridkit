package org.gridkit.lab.tentacle;

public interface Source<T extends MonitoringTarget> {

	public <E extends MonitoringTarget, S extends Source<E>> S find(SourceExpander<T, E, S> expander);
	
	public <S extends Source<?>> S hosts(Class<S> hostType);
	
	public SamplerHost<? extends Source<T>, T> known();

	public SamplerHost<? extends Source<T>, T> all();
	
}
