package org.gridkit.gatling.remoting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.gridkit.fabric.exec.ExecCommand;
import org.gridkit.fabric.remoting.RmiGateway;
import org.gridkit.gatling.remoting.bootstraper.Bootstraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelForwardedTCPIP;
import com.jcraft.jsch.ForwardedTCPIPDaemon;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class ControlledHost {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControlledHost.class);
	
	private SSHFactory factory;
	private String host;
	private String agentHome; 
	private String javaExecPath = "java";
	
	private Session ssh;
	private RemoteFileCache remoteCache;
	private Random random = new Random();
	
	private String bootJarPath;
	private int controlPort;

	private Map<String, RemoteControlHandler> agents = new HashMap<String, RemoteControlHandler>(); 	
	
	public ControlledHost(String host, SSHFactory sshFactory) {
		this.host = host;
		this.factory = sshFactory;
	}
	
	public void setAgentHome(String agentHome) {
		this.agentHome = agentHome;
	}

	public void setJavaExecPath(String javaExecPath) {
		this.javaExecPath = javaExecPath;
	}

	public void init() throws JSchException, SftpException, IOException, InterruptedException {
		ssh = factory.getSession(host);
		remoteCache = new RemoteFileCache();
		remoteCache.setAgentHome(agentHome);
		remoteCache.setSession(ssh);
		remoteCache.init();

		initRemoteClassPath();
		
		initPortForwarding();
		
		ExecCommand halloWorldCmd = new ExecCommand(javaExecPath);
//		ExecCommand halloWorldCmd = new ExecCommand("echo");
		halloWorldCmd.setWorkDir(agentHome);
		halloWorldCmd.addArg("-jar").addArg(bootJarPath);
		RemoteProcess rp = new RemoteProcess(ssh, halloWorldCmd);
		rp.getOutputStream().close();
		BackgroundStreamDumper.link(rp.getInputStream(), System.out);
		BackgroundStreamDumper.link(rp.getErrorStream(), System.err);
		
		rp.waitFor();
		Thread.sleep(2000);
	}

	private synchronized void initPortForwarding() {
		for(int i = 0; i != 10; ++i) {
			int port;
			try {
				port = 50000 + random.nextInt(1000);
				ssh.setPortForwardingR(port, ControlledHost.class.getName() + "$PortForwardAcceptor", new Object[]{agents});
			}
			catch(JSchException e) {
				LOGGER.warn("Failed to forward port " + e.toString());
				continue;
			}
			controlPort = port;
		}
		throw new RuntimeException("Failed to bind remote port");
	}

	private void initRemoteClassPath() throws IOException, SftpException {
		StringBuilder remoterClasspath = new StringBuilder();
		for(URL url: ClasspathUtils.listCurrentClasspath()) {
			byte[] data;
			String lname;
			try {
				File file = new File(url.toURI());
				if (file.isFile()) {
					data = readFile(file);
					lname = file.getName();
				}
				else {
					lname = file.getName();
					if ("classes".equals(lname)) {
						lname = file.getParentFile().getName();
					}
					if ("target".equals(lname)) {
						lname = file.getParentFile().getParentFile().getName();
					}
					lname += ".jar";
					data = ClasspathUtils.jarFiles(file.getPath());
				}
			}
			catch(Exception e) {
				LOGGER.warn("Cannot copy to remote host URL " + url.toString(), e);
				continue;
			}
			String name = remoteCache.upload(lname, data);
			if (remoterClasspath.length() > 0) {
				remoterClasspath.append(' ');
			}
			remoterClasspath.append(name);
		}
		Manifest mf = new Manifest();
		mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mf.getMainAttributes().put(Attributes.Name.CLASS_PATH, remoterClasspath.toString());
		mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, Bootstraper.class.getName());
		
		byte[] bootJar = ClasspathUtils.createManifestJar(mf);
		bootJarPath = remoteCache.upload("booter.jar", bootJar);
	}
	
	private byte[] readFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamHelper.copy(fis, bos);
		bos.close();
		return bos.toByteArray();
	}
	
	private class RemoteControlHandler {

		private RemoteProcess remoteProcess;		
		private PortForwardAcceptor acceptor;
		private RmiGateway gateway;
		
		public void connected(InputStream in, OutputStream out, PortForwardAcceptor acceptor) throws IOException {
			this.gateway = new RmiGateway();
			this.acceptor = acceptor;
			gateway.connect(in, out);
		}
		
		void disconnected() {
			acceptor = null;
			close();
		}
		
		public void close() {
			gateway.shutdown();
			remoteProcess.destroy();
			if (acceptor != null) {
				acceptor.close();
			}
		} 
	}
	
	private static class PortForwardAcceptor implements ForwardedTCPIPDaemon {

		private Map<String, RemoteControlHandler> agents;
		
		private ChannelForwardedTCPIP channel;
		private RemoteControlHandler handler;
		private InputStream in;
		private OutputStream out;
		
		private boolean verified;
		
		@Override
		public void setArg(Object[] arg) {
			agents = (Map<String, RemoteControlHandler>) arg[0];
		}

		@Override
		public void setChannel(ChannelForwardedTCPIP channel, InputStream in, OutputStream out) {
			this.channel = channel;
			verifyMagic(in);
			this.in = in;
			this.out = out;
		}

		@Override
		public void run() {
			if (!verified) {
				LOGGER.warn("Connection verfication failed");
				close();
			}
			
			try {
				synchronized(this) {					
					handler.connected(in, out, this);
				}
			}
			catch(Exception e) {
				LOGGER.error("Channel failure", e);
				close();
			}
		}

		private void close() {
			try {
				in.close();
			} catch (IOException e) {
				// ignore
			}				
			try {
				out.close();
			} catch (IOException e) {
				// ignore
			}
			channel.disconnect();
			if (handler != null) {
				handler.disconnected();
			}
		}

		private void verifyMagic(InputStream is) {
			try {
				byte[] magic = new byte[32];
				for(int i = 0; i != magic.length; ++i) {
					magic[i] = (byte) is.read();
				}
				String key = new String(magic);
				RemoteControlHandler rch = agents.get(key);
				if (rch != null) {
					handler = rch;
					verified = true;
				}
				else {
					LOGGER.error("Invalid connection ID " + key);
					close();
				}
			}
			catch(IOException e) {
				return;
			}			
		}
	}
}
