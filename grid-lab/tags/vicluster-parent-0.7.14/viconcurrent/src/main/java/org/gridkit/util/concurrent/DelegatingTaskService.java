/**
 * Copyright 2012 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.util.concurrent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This class is mostly usefully for mass canceling of tasks.
 *
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class DelegatingTaskService implements TaskService, TaskService.Component {

	private final TaskService delegate;
	private final Set<TaskWrapper> tasks = new HashSet<TaskWrapper>();
	
	private boolean terminated;
	
	public DelegatingTaskService(TaskService delegate) {
		this.delegate = delegate;
	}

	@Override
	public void schedule(Task task) {
		TaskWrapper wrapper = new TaskWrapper(task);
		enqueue(wrapper, 0, TimeUnit.NANOSECONDS);
	}

	@Override
	public void schedule(Task task, long delay, TimeUnit tu) {
		TaskWrapper wrapper = new TaskWrapper(task);
		enqueue(wrapper, delay, tu);
	}

	private void enqueue(TaskWrapper wrapper, long delay, TimeUnit tu) {
		synchronized (this) {
			if (!terminated) {
				tasks.add(wrapper);
				delegate.schedule(wrapper, delay, tu);
				return;
			}			
		}
		wrapper.abort();
	}

	@Override
	public void shutdown() {
		Set<TaskWrapper> tasks;
		synchronized(this) {
			if (terminated) {
				return;
			}
			terminated = true;
			tasks = new HashSet<TaskWrapper>(this.tasks);
		}
		for(TaskWrapper task: tasks) {
			task.abort();
		}
		synchronized (this) {
			for(TaskWrapper task: new HashSet<TaskWrapper>(this.tasks)) {
				task.abort();
			}
			tasks.clear();
		}
	}
	
	synchronized void removeTask(TaskWrapper wrapper) {
		tasks.remove(wrapper);
	}
	
	private class TaskWrapper implements Task {

		private final Task task;

		private Thread execThread;
		private boolean started = false;
		private boolean canceled = false;
		private boolean finished = false;
		
		public TaskWrapper(Task task) {
			this.task = task;
		}
		
		@Override
		public void run() {
			synchronized (this) {
				if (canceled) {
					return;
				}
				else {
					started = true;
					execThread = Thread.currentThread();
				}
				
			}
			try {
				task.run();
			}
			finally {
				synchronized (this) {
					execThread = null;
					finished = true;
				}
			}
			removeTask(this);
		}

		@Override
		public void interrupt(Thread taskThread) {
			synchronized (this) {
				if (canceled || finished) {
					return;
				}
			}
			task.interrupt(taskThread);			
		}

		@Override
		public void cancled() {
			synchronized (this) {
				if (canceled) {
					return;
				}
				canceled = true;
			}
			removeTask(this);
			task.cancled();
		}
		
		public void abort() {
			synchronized (this) {
				if (finished || canceled) {
					return;
				}
				canceled = true;
				if (started) {
					interrupt(execThread);
					return;
				}
			}
			try {
				task.cancled();
			}
			catch(Exception e) {
				// ignore
			}
		}
	}
}
