package org.gridkit.lab.tentacle;

public class MonitoringSchema implements Source<Experiment> {

	@Override
	public <E extends MonitoringTarget, S extends Source<E>> S find(
			SourceExpander<Experiment, E, S> expander) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Source<?>> S hosts(Class<S> hostType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable<? extends Source<Experiment>, Experiment> known() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable<? extends Source<Experiment>, Experiment> all() {
		// TODO Auto-generated method stub
		return null;
	}

	public class Config {
		
		
		
		
	}
}
