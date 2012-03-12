package org.gridkit.gatling.remoting;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public interface SSHFactory {

	public Session getSession(String host) throws JSchException;
	
}
