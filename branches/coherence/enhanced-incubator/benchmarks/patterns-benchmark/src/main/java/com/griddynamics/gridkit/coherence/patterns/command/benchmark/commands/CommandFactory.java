/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
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
package com.griddynamics.gridkit.coherence.patterns.command.benchmark.commands;


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
