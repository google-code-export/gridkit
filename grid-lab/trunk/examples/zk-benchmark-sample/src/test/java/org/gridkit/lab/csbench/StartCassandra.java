package org.gridkit.lab.csbench;

import org.apache.cassandra.service.CassandraDaemon;
import org.junit.Test;

public class StartCassandra {

	@Test
	public void go() {
		
		System.setProperty("cassandra.config", "cassandra/cassandra.yaml");		
		System.setProperty("cassandra-foreground", "true");		
		CassandraDaemon daemon = new CassandraDaemon();
		daemon.activate();		
	}
	
}
