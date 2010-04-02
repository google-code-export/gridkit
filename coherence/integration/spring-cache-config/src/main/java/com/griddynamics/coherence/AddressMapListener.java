package com.griddynamics.coherence;

import com.tangosol.util.AbstractMapListener;
import com.tangosol.util.MapEvent;

/**
 * Created by IntelliJ IDEA.
 * User: stryuber
 * Date: 01.04.2010
 * Time: 16:32:47
 * To change this template use File | Settings | File Templates.
 */
public class AddressMapListener extends AbstractMapListener {
    public void entryInserted(MapEvent event) {
        Address address = (Address) event.getNewEntry().getValue();
        System.out.println("Map listener detected new arrival:\n"+address);
    }
}
