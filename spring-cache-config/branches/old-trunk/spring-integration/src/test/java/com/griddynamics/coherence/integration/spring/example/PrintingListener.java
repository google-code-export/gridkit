package com.griddynamics.coherence.integration.spring.example;

import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

/**
 * @author Dmitri Babaev
 */
public class PrintingListener implements MapListener {

	public void entryDeleted(MapEvent paramMapEvent) {
		System.out.println(paramMapEvent);
	}

	public void entryInserted(MapEvent paramMapEvent) {
		System.out.println(paramMapEvent);
	}

	public void entryUpdated(MapEvent paramMapEvent) {
		System.out.println(paramMapEvent);
	}
}
