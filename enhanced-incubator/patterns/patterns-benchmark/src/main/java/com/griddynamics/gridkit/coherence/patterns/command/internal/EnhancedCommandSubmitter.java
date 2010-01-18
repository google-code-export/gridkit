package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.util.Arrays;
import java.util.Map;

import com.griddynamics.gridkit.coherence.patterns.command.ContextConfigurationScheme;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.identifiers.StringBasedIdentifier;
import com.oracle.coherence.common.ticketing.Ticket;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.CommandSubmitter;
import com.oracle.coherence.patterns.command.Context;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class EnhancedCommandSubmitter implements CommandSubmitter {
	
	private Map<String, ContextConfigurationScheme> schemes;
	private Map<String, String> contextMapping;
	private IdGenerator<Long> commandIdGenerator;
	
	public EnhancedCommandSubmitter() {
		schemes = EnhancedPatternsHelper.getSchemes();
		contextMapping = EnhancedPatternsHelper.getContextMapping();
		commandIdGenerator = EnhancedPatternsHelper.getCommandIdGenerator();
	}

	@Override
	public <C extends Context> Identifier submitCommand(Identifier ctxId, Command<C> command) {
		// TODO temporary implementation
		String contextKey = ((StringBasedIdentifier)ctxId).getString();		
		ContextConfigurationScheme scheme = schemes.get(contextMapping.get(contextKey));
		
		NamedCache cache = CacheFactory.getCache(scheme.getCommandCacheName());
		
		SubmitMessageProcessor smp = new SubmitMessageProcessor();
		Long uid = commandIdGenerator.nextId();
		
		CommandRefKey refKey = new CommandRefKey(contextKey, uid);
		CommandBodyKey bodyKey = new CommandBodyKey(contextKey, uid);		
		smp.addCommand(contextKey, uid, command);
		
		return (Ticket)cache.invokeAll(Arrays.asList(refKey, bodyKey), smp).get(refKey);
	}

	@Override
	public <C extends Context> Identifier submitCommand(Identifier ctxId, Command<C> command, boolean allowSubmissionWhenContextDoesNotExist) {
		return submitCommand(ctxId, command);
	}

	@Override
	public <C extends Context> boolean cancelCommand(Identifier commandIdentifier) {
		// TODO not implemented
		return false;
	}
}
