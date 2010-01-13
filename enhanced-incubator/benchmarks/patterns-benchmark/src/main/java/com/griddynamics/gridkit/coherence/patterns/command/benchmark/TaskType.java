package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import com.oracle.coherence.patterns.command.Command;

public interface TaskType
{
	public Command<SimpleTestContext> createCommand(long id, String reportBuffer);
	public int getMarksPerTask();
	
	public static class EmptyTaskType implements TaskType {

		@Override
		public Command<SimpleTestContext> createCommand(long id, String reportBuffer) {
			return new EmptyCommand(id, reportBuffer);
		}
		
		@Override
		public int getMarksPerTask() {			
			return 1;
		}
	}

	public static class ReadTaskType implements TaskType {
		
		@Override
		public Command<SimpleTestContext> createCommand(long id, String reportBuffer) {
			return new ReadCommand(id, reportBuffer);
		}
		
		@Override
		public int getMarksPerTask() {			
			return 1;
		}
	}

	public static class UpdateTaskType implements TaskType {
		
		@Override
		public Command<SimpleTestContext> createCommand(long id, String reportBuffer) {
			return new UpdateCommand(id, reportBuffer);
		}
		
		@Override
		public int getMarksPerTask() {			
			return 1;
		}
	}
}