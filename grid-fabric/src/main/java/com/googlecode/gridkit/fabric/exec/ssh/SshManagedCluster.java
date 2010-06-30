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
package com.googlecode.gridkit.fabric.exec.ssh;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.gridkit.fabric.exec.ExecCommand;
import com.griddynamics.convergence.demo.utils.cluster.Cluster;
import com.griddynamics.convergence.demo.utils.cluster.GridHostMap;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshManagedCluster implements Cluster {

	private final Map<String, SshManagedHost> hosts = new HashMap<String, SshManagedHost>();
	private final GridHostMap hostMap;
	private final SshSessionFactory sshFactory;

	public SshManagedCluster(String[] hosts, SshSessionFactory sshFactory) throws IOException  {
		this.hostMap = new GridHostMap(hosts);
		this.sshFactory = sshFactory;
		
		for(String host: hosts) {
			try {
				this.hosts.put(host, new SshManagedHost(host));
			}
			catch(JSchException e) {
				throw new SshException("Failed to connect " + host, e);
			}
		}
	}

	public Host getHost(String address) {
		Host host = hosts.get(normalizeHostname(address));
		
		if (host == null) {
			throw new IllegalArgumentException("Unknown host: " + address);
		}
		
		return host;
	}
	
	public String normalizeHostname(String hostname) {
		return hostMap.normalizeHost(hostname);
	}

	public Host[] getNodes() {
		return hosts.values().toArray(new Host[hosts.size()]);
	}
	
	private class SshManagedHost implements Host {

		private final String host;
		private final Session sftSession;
		
		public SshManagedHost(String host) throws JSchException {
			this.host = host;
			this.sftSession = sshFactory.connect(host);
		}
		
		public String getHostname() {
			return host;
		}

		public Process execute(ExecCommand command) throws IOException {
			try {
				return new RemoteSshProcess(sshFactory.connect(host), command);
			} catch (JSchException e) {
				throw new SshException(e);
			}
		}
	}
}
