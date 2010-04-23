package com.griddynamics.coherence.integration.spring;

import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

public class PrintingListener implements MapListener {

	public void entryDeleted(MapEvent paramMapEvent) {
		System.out.println(String.format("entry '%s' is deleted", paramMapEvent.getKey()));
	}

	public void entryInserted(MapEvent paramMapEvent) {
		System.out.println(String.format("entry '%s':'%s' is inserted", paramMapEvent.getKey(), paramMapEvent.getNewValue()));
	}

	public void entryUpdated(MapEvent paramMapEvent) {
		System.out.println(String.format("entry '%s' was updated; old value: '%s', new value: '%s'",
				paramMapEvent.getKey(), paramMapEvent.getOldValue(), paramMapEvent.getNewValue()));
	}
}
