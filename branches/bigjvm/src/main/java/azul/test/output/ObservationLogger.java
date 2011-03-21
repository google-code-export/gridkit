package azul.test.output;

public interface ObservationLogger {
	void logObservation(long timestamp, long data);
	
	void close();
}
