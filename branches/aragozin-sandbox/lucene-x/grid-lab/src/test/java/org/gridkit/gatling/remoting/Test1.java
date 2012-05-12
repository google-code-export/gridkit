package org.gridkit.gatling.remoting;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class Test1 {

	
	public static void main(String[] args) throws JSchException, SftpException, IOException, InterruptedException {

		
		ControlledHost host = initHost_u1();
		
		host.init();
		
		ExecutorService es1 = host.createRemoteExecutor();
		es1.submit(new Echo("Y-a-a-ho!"));

		ExecutorService es2 = host.createRemoteExecutor();
		es2.submit(new Echo("Y-a-a-ho!"));
		
		Thread.sleep(5000);
		
		System.exit(1);
	}
	
	private static ControlledHost initHost() {
		SSHFactory sshFactory = new GrimsSSHFactory();
		
		ControlledHost host = new ControlledHost("longmltsreu9.uk.db.com", sshFactory);
		host.setAgentHome("/local/applogs/datatgramtest-20111023-0803");
		host.setJavaExecPath("/apps/grimis/java/linux/jdk1.6.0_22/jre/bin/java");
		return host;
	}

	private static ControlledHost initHost_u1() {
		DefaultSSHFactory sshFactory = new DefaultSSHFactory();
		
		sshFactory.setUser("coreserv");
		sshFactory.setPassword("l0nd0n99");
		
		ControlledHost host = new ControlledHost("longmrdfappu1.uk.db.com", sshFactory);
		host.setAgentHome("/mnt/ramfs/qmt");
		host.setJavaExecPath("/usr/lib64/jvm/java-1.6.0-sun/bin/java");
		return host;
	}

	public static class Echo implements Callable<Void>, Serializable {

		private String sound;
		
		public Echo(String sound) {
			this.sound = sound;
		}

		@Override
		public Void call() throws Exception {
			System.out.println(sound);
			return null;
		}
	}
}
