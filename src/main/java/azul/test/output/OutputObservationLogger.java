package azul.test.output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class OutputObservationLogger implements ObservationLogger {
	private Writer writer;
	private BlockingQueue<Task> queue;
	
	private int sampleSize;
	
	protected List<Long> data;
	protected List<Long> timestamp;
	
	public OutputObservationLogger(String fileName, BlockingQueue<Task> queue, int sampleSize, int bufferSize) throws IOException {
		Writer out = new FileWriter(fileName);
		writer = new BufferedWriter(out, bufferSize * 1024 * 1024);
		
		this.queue = queue;
		this.sampleSize = sampleSize;
		
		data = new ArrayList<Long>(sampleSize);
		timestamp = new ArrayList<Long>(sampleSize);
	}
	
	@Override
	public void logObservation(long timestamp, long data) {
		this.data.add(data);
		this.timestamp.add(timestamp);
		
		if (this.data.size() == sampleSize) {
			queue.add(new OutputTask(writer, this.data, this.timestamp, false));
			
			this.data = new ArrayList<Long>(sampleSize);
			this.timestamp = new ArrayList<Long>(sampleSize);
		}
	}
	
	@Override
	public void close() {
		queue.add(new OutputTask(writer, this.data, this.timestamp, true));
	}
}
