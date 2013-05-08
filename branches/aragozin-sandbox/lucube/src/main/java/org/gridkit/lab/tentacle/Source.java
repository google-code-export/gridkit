package org.gridkit.lab.tentacle;

public interface Source<T extends MonitoringTarget> {

	public <E extends MonitoringTarget, S extends Source<E>> S at(Locator<T, E, S> locator);
	
	public <S extends Source<?>> S hosts(Class<S> hostType);
	
}
