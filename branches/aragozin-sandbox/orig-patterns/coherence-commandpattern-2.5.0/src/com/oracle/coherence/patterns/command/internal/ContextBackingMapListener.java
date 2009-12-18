/*
 * File: ContextBackingMapListener.java
 * 
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.
 * 
 * Oracle is a registered trademark of Oracle Corporation and/or its
 * affiliates.
 * 
 * This software is the confidential and proprietary information of Oracle
 * Corporation. You shall not disclose such confidential and proprietary
 * information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Oracle Corporation.
 * 
 * Oracle Corporation makes no representations or warranties about 
 * the suitability of the software, either express or implied, 
 * including but not limited to the implied warranties of 
 * merchantability, fitness for a particular purpose, or 
 * non-infringement.  Oracle Corporation shall not be liable for 
 * any damages suffered by licensee as a result of using, modifying 
 * or distributing this software or its derivatives.
 * 
 * This notice may not be removed or altered.
 */
package com.oracle.coherence.patterns.command.internal;

import java.util.concurrent.TimeUnit;

import com.oracle.coherence.common.backingmaplisteners.AbstractMultiplexingBackingMapListener;
import com.oracle.coherence.common.backingmaplisteners.Cause;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.patterns.command.Context;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.util.MapEvent;

/**
 * <p>A {@link ContextBackingMapListener} is responsible for starting
 * and stopping {@link CommandExecutor}s for {@link Context}s as they are 
 * inserted and removed from the {@link ContextWrapper#CACHENAME} cache
 * respectively.</p>
 *
 * @author Brian Oliver
 */
public class ContextBackingMapListener extends AbstractMultiplexingBackingMapListener {
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param backingMapManagerContext
	 */
	public ContextBackingMapListener(BackingMapManagerContext backingMapManagerContext) {
		super(backingMapManagerContext);
	}

	
	/**
	 * {@inheritDoc}
	 */
 	public void onBackingMapEvent(MapEvent mapEvent, final Cause cause) {
 		
		if (mapEvent.getId() == MapEvent.ENTRY_INSERTED) {
	 		Identifier contextIdentifier = (Identifier)mapEvent.getKey();
			if (Logger.isEnabled(Logger.DEBUG)) Logger.log(Logger.DEBUG, "Context %s has been inserted into this member", contextIdentifier);

			final CommandExecutor commandExecutor = CommandExecutorManager.ensureCommandExecutor(contextIdentifier, getContext());

			if (Logger.isEnabled(Logger.DEBUG)) Logger.log(Logger.DEBUG, "Scheduling ContextExecutor for %s to start", contextIdentifier);
			CommandExecutorManager.schedule(new Runnable() {
				public void run() {
					commandExecutor.start();				
				}
			}, 0, TimeUnit.SECONDS);
			
		} else if (mapEvent.getId() == MapEvent.ENTRY_DELETED) {
	 		Identifier contextIdentifier = (Identifier)mapEvent.getKey();
			if (Logger.isEnabled(Logger.DEBUG)) Logger.log(Logger.DEBUG, "Context %s has been removed from this member", contextIdentifier);

			final CommandExecutor commandExecutor = CommandExecutorManager.getCommandExecutor(contextIdentifier);
			if (commandExecutor != null) {
				commandExecutor.stop();				
			}
		}
	}
}
