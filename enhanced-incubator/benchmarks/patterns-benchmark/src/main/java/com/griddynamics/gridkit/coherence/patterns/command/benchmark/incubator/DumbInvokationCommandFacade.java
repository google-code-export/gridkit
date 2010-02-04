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
package com.griddynamics.gridkit.coherence.patterns.command.benchmark.incubator;

import java.io.IOException;
import java.io.Serializable;

import com.griddynamics.gridkit.coherence.patterns.command.benchmark.PatternFacade;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.identifiers.StringBasedIdentifier;
import com.oracle.coherence.common.ticketing.Ticket;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ContextConfiguration;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

public class DumbInvokationCommandFacade implements PatternFacade {

	NamedCache cache;

	public DumbInvokationCommandFacade() {
		cache = CacheFactory.getCache("context");
	}
	
	@Override
	public Identifier registerContext(String name, Context ctx) {
		cache.put(name, ctx);
		return StringBasedIdentifier.newInstance(name);
	}

	@Override
	public <T extends Context> Identifier submit(Identifier id, Command<T> command) {
		String name = ((StringBasedIdentifier)id).getString();
		cache.invoke(name, new CommandProcessor<T>(command));
		return null;
	}
	
	public static class CommandProcessor<T extends Context> extends AbstractProcessor implements ExecutionEnvironment<T>, Serializable, PortableObject {

		private static final long serialVersionUID = 20100106L;
		
		private Command<T> command;
		private Entry entry;
		
		public CommandProcessor() {
			// for POF
		}
		
		public CommandProcessor(Command<T> command) {
			this.command = command;
		}

		@Override
		public T getContext() {
			return (T)entry.getValue();
		}

		@Override
		public ContextConfiguration getContextConfiguration() {
			return null;
		}

		@Override
		public Identifier getContextIdentifier() {
			return StringBasedIdentifier.newInstance((String) entry.getKey());
		}

		@Override
		public Ticket getTicket() {
			return null;
		}

		@Override
		public boolean hasCheckpoint() {
			return false;
		}

		@Override
		public boolean isRecovering() {
			return false;
		}

		@Override
		public Object loadCheckpoint() {
			return null;
		}

		@Override
		public void removeCheckpoint() {
			// TODO Auto-generated method stub
		}

		@Override
		public void saveCheckpoint(Object state) {
		}

		@Override
		public void setContext(T context) {
			entry.setValue(context);
		}

		@Override
		public Object process(Entry entry) {
			this.entry = entry;
			command.execute(this);
			return null;
		}

		@Override
		public void readExternal(PofReader in) throws IOException {
			int propId = 0;
			command = (Command<T>) in.readObject(propId++);
		}

		@Override
		public void writeExternal(PofWriter out) throws IOException {
			int propId = 0;
			out.writeObject(propId++, command);
		}
	}

	@Override
	public InvocationService getInvocationService() {
		// TODO Auto-generated method stub
		return null;
	}
}
