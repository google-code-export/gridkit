/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.griddynamics.convergence.demo.utils.cluster;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class GridHostMap implements Serializable {

	private static final long serialVersionUID = 20090420L;
	
	private String[] servers;
	
	private transient Map<String, String> host2host = new HashMap<String, String>();
	private transient Map<String, String> ip2host = new HashMap<String, String>();
	private transient Map<String, String> host2ip = new HashMap<String, String>();

	public GridHostMap(String[] servers) throws UnknownHostException {
		this.servers = servers;
		init();
	}

	private void init() throws UnknownHostException {
		host2host = new HashMap<String, String>();
		ip2host = new HashMap<String, String>();
		host2ip = new HashMap<String, String>();
		for(String server: servers) {
			
			host2host.put(server, server);
			InetAddress[] addrs = InetAddress.getAllByName(server);
			for(InetAddress addr: addrs) {
				ip2host.put(addr.getHostAddress(), server);
			}
		}
	}
	
	public String[] getKnownHosts() {
		return ip2host.values().toArray(new String[0]);
	}
	
	public String normalizeHost(String host) {
		if (host2host.containsKey(host)) {
			return host2host.get(host);
		}
		
		try {
			InetAddress addr = InetAddress.getByName(host);
			String ip = addr.getHostAddress();
			
			if (ip2host.containsKey(ip)) {
				String nhost = ip2host.get(ip);
				host2host.put(host, nhost);
				return nhost;
			}
		} catch (UnknownHostException e) {
			// ignore
		}
		
		//System.out.println("Failed to normalize hostname '" + host + "'");
		return null;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		init();
	}
}
