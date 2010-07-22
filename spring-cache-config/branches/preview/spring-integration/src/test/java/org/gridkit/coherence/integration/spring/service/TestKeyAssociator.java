package org.gridkit.coherence.integration.spring.service;

import org.junit.Ignore;

import com.tangosol.net.PartitionedService;
import com.tangosol.net.partition.KeyAssociator;

@Ignore
public class TestKeyAssociator implements KeyAssociator {

	public TestKeyAssociator() {
		new String();
	}
	
	@Override
	public Object getAssociatedKey(Object paramObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(PartitionedService paramPartitionedService) {
		// TODO Auto-generated method stub
		
	}

}
