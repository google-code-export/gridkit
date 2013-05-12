package org.gridkit.lab.tentacle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.gridkit.lab.tentacle.ObservationHost.ObservationActivity;
import org.gridkit.util.concurrent.DelegatingTaskService;
import org.gridkit.util.concurrent.TaskService;

public class BasicMonitoringTarget implements MonitoringTarget {

	private ObservationHost host;
	private TaskService sharedScheduler;
	private BoundTaskService boundScheduler;
	
	public BasicMonitoringTarget(ObservationHost host, TaskService sharedScheduler) {
		this.host = host;
		this.sharedScheduler = sharedScheduler;
		this.boundScheduler = new BoundTaskService(sharedScheduler) ;
		this.host.addActivity(new ObservationActivity() {
			@Override
			public void start() {
				boundScheduler.start();
			}

			@Override
			public void stop() {
				boundScheduler.shutdown();
			}
		});
	}
	
	@Override
	public TaskService getSharedTaskService() {
		return sharedScheduler;
	}

	@Override
	public TaskService getBoundTaskService() {
		return boundScheduler;
	}

	@Override
	public ObservationHost getObservationHost() {
		return host;
	}


	private static class BoundTaskService extends DelegatingTaskService {

		private boolean started;
		private List<DeferedTask> queue = new ArrayList<DeferedTask>();
		
		public BoundTaskService(TaskService delegate) {
			super(delegate);
		}

		private synchronized void start() {
			started = true;
			for (DeferedTask task: queue) {
				schedule(task.task, task.delay, task.tu);
			}
			queue = null;
		}
		
		@Override
		public void schedule(Task task) {
			if (started) {
				super.schedule(task);
			}
			else {
				schedule(task, 0, TimeUnit.MILLISECONDS);
			}
		}

		@Override
		public void schedule(Task task, long delay, TimeUnit tu) {
			if (started) {
				super.schedule(task, delay, tu);
			}
			else {
				defer(task, delay, tu);
			}
		}
		
		private synchronized void defer(Task task, long delay, TimeUnit tu) {
			if (started) {
				super.schedule(task, delay, tu);
			}
			else {
				queue.add(new DeferedTask(task, delay, tu));
			}			
		}

		private static class DeferedTask {
			
			private Task task;
			private long delay;
			private TimeUnit tu;
			
			public DeferedTask(Task task, long delay, TimeUnit tu) {
				this.task = task;
				this.delay = delay;
				this.tu = tu;
			}
		}
	}
	
}
