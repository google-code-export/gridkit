package com.griddynamics.gridkit.coherence.benchmark.capacity;

import com.tangosol.net.Action;
import com.tangosol.net.ActionPolicy;
import com.tangosol.net.Service;

public class TestQuorum implements ActionPolicy {

    @Override
    public void init(Service service) {
    }

    @Override
    public boolean isAllowed(Service service, Action action) {
        return true;
    }
}
