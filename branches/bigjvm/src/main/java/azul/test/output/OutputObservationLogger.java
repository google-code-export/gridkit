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
	
	protected long[] data;
    protected int datasize;
	protected long[] timestamp;
    protected int timestampsize;
	
	public OutputObservationLogger(String fileName, BlockingQueue<Task> queue, int sampleSize, int bufferSize) throws IOException {
		Writer out = new FileWriter(fileName);
		writer = new BufferedWriter(out, bufferSize * 1024 * 1024);
		
		this.queue = queue;
		this.sampleSize = sampleSize;
		
		data = new long[sampleSize];
		timestamp = new long[sampleSize];
	}
	
	@Override
	public void logObservation(long timestamp, long data) {
		this.data[datasize++] = data;
		this.timestamp[timestampsize++] = timestamp;
		
		if (datasize == sampleSize) {
			queue.add(new OutputTask(writer, this.data, datasize, this.timestamp, timestampsize, false));
			
			this.data = new long[sampleSize];
			this.timestamp = new long[sampleSize];
            datasize = 0;
            timestampsize = 0;
		}
	}
	
	@Override
	public void close() {
		queue.add(new OutputTask(writer, this.data, datasize, this.timestamp, timestampsize, true));
	}
}
