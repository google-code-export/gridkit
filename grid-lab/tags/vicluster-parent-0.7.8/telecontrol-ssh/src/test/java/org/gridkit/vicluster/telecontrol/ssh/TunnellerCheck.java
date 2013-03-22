package org.gridkit.vicluster.telecontrol.ssh;

import java.util.HashMap;
import java.util.Map;

import org.gridkit.vicluster.telecontrol.BackgroundStreamDumper;
import org.gridkit.vicluster.telecontrol.ControlledProcess;
import org.gridkit.vicluster.telecontrol.JvmConfig;
import org.junit.Test;

public class TunnellerCheck {

	public Map<String, String> config() {
		Map<String, String> config = new HashMap<String, String>();
		config.put(RemoteNodeProps.HOST, "cbox1");
		config.put(RemoteNodeProps.ACCOUNT, "root");
		config.put(RemoteNodeProps.PASSWORD, "toor");
		config.put(RemoteNodeProps.JAVA_EXEC, "java");
		return config;
	}
	
	@Test
	public void test_init() throws Exception {
		
		TunnellerJvmReplicator per = new TunnellerJvmReplicator();
		per.configure(config());
		per.init();
		
		ControlledProcess cp = per.createProcess("test", new JvmConfig());
		BackgroundStreamDumper.link(cp.getProcess().getInputStream(), System.out);
		BackgroundStreamDumper.link(cp.getProcess().getErrorStream(), System.err);
		cp.getExecutionService().submit(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("This is out");
				System.err.println("This is err");				
			}
		});
		
		Thread.sleep(500);
	}
	
}
