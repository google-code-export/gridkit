package com.griddynamics.coherence.integration.spring;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

/**
 * @author Dmitri Babaev
 */
public class HistoryListener implements MapListener {
	List<MapEvent> history;

	public void entryDeleted(MapEvent paramMapEvent) {
		history.add(paramMapEvent);
	}
	
	public void entryInserted(MapEvent paramMapEvent) {
		history.add(paramMapEvent);
	}
	
	public void entryUpdated(MapEvent paramMapEvent) {
		history.add(paramMapEvent);
	}
	
	@Required
	public void setHistory(List<MapEvent> history) {
		this.history = history;
	}
}
