package com.griddynamics.gridkit.coherence.patterns.command;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ContextConfiguration;
import com.oracle.coherence.patterns.command.ContextsManager;
import com.tangosol.util.ValueExtractor;

class EhContextsManger implements ContextsManager {

	@Override
	public Object extractValueFromContext(Identifier identifier,
			ValueExtractor valueExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Context getContext(Identifier identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Identifier registerContext(Context context,
			ContextConfiguration contextConfiguration) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Identifier registerContext(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Identifier registerContext(Identifier identifier, Context context,
			ContextConfiguration contextConfiguration) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Identifier registerContext(Identifier identifier, Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Identifier registerContext(String contextName, Context context,
			ContextConfiguration contextConfiguration) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Identifier registerContext(String contextName, Context context) {
		// TODO Auto-generated method stub
		return null;
	}
}
