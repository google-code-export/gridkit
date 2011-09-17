package org.gridkit.search.gemfire.benchmark.task;

import org.gridkit.search.gemfire.benchmark.model.Commitment;

import java.util.Iterator;

public abstract class PositionKeyTask extends BenchmarkTask {
    Iterator<String> positionKeys;

    @Override
    public void reset() {
        super.reset();
        positionKeys = ftsData.getPositionKeys().iterator();
    }

    @Override
    public boolean execute() throws Exception {
        if (!positionKeys.hasNext())
            return false;

        getCommitment(positionKeys.next());

        return positionKeys.hasNext();
    }

    protected abstract Commitment getCommitment(String positionKey) throws Exception;
}
