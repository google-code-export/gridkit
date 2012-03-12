package org.gridkit.gatling.remoting;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class Test {

	
	public static void main(String[] args) throws JSchException, SftpException, IOException, InterruptedException {
		
		SSHFactory sshFactory = new GrimsSSHFactory();
		
		ControlledHost host = new ControlledHost("longmltsreu9.uk.db.com", sshFactory);
		host.setAgentHome("/local/applogs/datatgramtest-20111023-0803");
		host.setJavaExecPath("/apps/grimis/java/linux/jdk1.6.0_22/jre/bin/java");
		
		host.init();
		
	}
}
