package com.griddynamics.gridkit.coherence.patterns.command.benchmark;


public interface CommandFactory
{
	public BenchmarkCommand createCommand(long id, String reportBuffer);
	public int getMarksPerCommand();
	
	public static class EmptyCommandFactory implements CommandFactory {

		@Override
		public BenchmarkCommand createCommand(long id, String reportBuffer) {
			return new EmptyCommand(id, reportBuffer);
		}
		
		@Override
		public int getMarksPerCommand() {			
			return 1;
		}
	}

	public static class ReadCommandFactory implements CommandFactory {
		
		@Override
		public BenchmarkCommand createCommand(long id, String reportBuffer) {
			return new ReadCommand(id, reportBuffer);
		}
		
		@Override
		public int getMarksPerCommand() {			
			return 1;
		}
	}

	public static class UpdateCommandFactory implements CommandFactory {
		
		@Override
		public BenchmarkCommand createCommand(long id, String reportBuffer) {
			return new UpdateCommand(id, reportBuffer);
		}
		
		@Override
		public int getMarksPerCommand() {			
			return 1;
		}
	}
}