package azul.test.output;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class OutputWriter implements Callable<Void> {
	private final BlockingQueue<OutputTask> queue;
	
	public OutputWriter(BlockingQueue<OutputTask> queue) {
		this.queue = queue;
	}
	
	@Override
	public Void call() throws Exception {
		while (!Thread.interrupted()) {
			OutputTask task = queue.take();
			
			for (int i = 0; i < task.data.size(); ++i)
				task.writer.write(task.timestamp.get(i) + ", " + task.data.get(i) + "\n");
			
			if (task.isLast) {
				task.writer.flush();
				task.writer.close();
			}
		}
		
		return null;
	}
}
