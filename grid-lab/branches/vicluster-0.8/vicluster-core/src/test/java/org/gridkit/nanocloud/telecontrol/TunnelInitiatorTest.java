package org.gridkit.nanocloud.telecontrol;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.gridkit.zerormi.zlog.ZLogFactory;

public class TunnelInitiatorTest extends LocalControlConsoleTest {

	private LocalControlConsole lcon = new LocalControlConsole();
	private SimpleTunnelInitiator initiator = new SimpleTunnelInitiator(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java", "./target/tmp", ZLogFactory.getStdErrRootLogger());

	@Override
	public void initConsole() throws IOException, InterruptedException, TimeoutException {
		console = initiator.initTunnel(lcon);
	}
	
	@Override
	public void destroyConsole() {
		console.terminate();
		lcon.terminate();
	}
}
