package azul.test.output;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class OutputWriter implements Callable<Void> {
	public static volatile AtomicInteger filesClosed = new AtomicInteger(0);
	
	private final BlockingQueue<Task> queue;
	
	public OutputWriter(BlockingQueue<Task> queue) {
		this.queue = queue;
	}
	
	@Override
	public Void call() throws Exception {
		while (true) {
			Task task = queue.take();
			
			if (task.isPoison())
				break;
			
			OutputTask outputTask = (OutputTask)task;
			
			for (int i = 0; i < outputTask.datasize; ++i)
				outputTask.writer.write(outputTask.timestamp[i] + ", " + outputTask.data[i] + "\n");

			if (outputTask.isLast) {
				outputTask.writer.flush();
				outputTask.writer.close();
				filesClosed.incrementAndGet();
			}
		}
		
		return null;
	}
}
