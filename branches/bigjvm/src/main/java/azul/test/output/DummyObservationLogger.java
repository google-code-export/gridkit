package azul.test.output;

public class DummyObservationLogger implements ObservationLogger {
	@Override
	public void logObservation(long timestamp, long data) {}

	@Override
	public void close() {}
}
