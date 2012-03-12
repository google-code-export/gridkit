package org.gridkit.gatling.remoting;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.gridkit.fabric.exec.ssh.SshExecutor.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

public class RemoteExecutor {

	private Logger logger;
	private String host;
	private Session session; 
	
	public RemoteExecutor(String host, Session session) {
		logger = LoggerFactory.getLogger("remote.ssh.exec" + host );
		this.session = session;
	}
	
	private ExecutionResult execSync(String baseDir, String command) {
		
		int exitCode = 0;
		ByteArrayOutputStream errStr = new ByteArrayOutputStream();
		ByteArrayOutputStream outStr = new ByteArrayOutputStream();
		try {
			
			String rcommand = command;
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			if (baseDir != null) {
				rcommand = "cd " + baseDir + ";" + command;
			}
			channel.setCommand(rcommand);
			channel.setInputStream(null);
			
			((ChannelExec)channel).setErrStream(errStr);
			
			InputStream in=channel.getInputStream();
			long startTime = System.nanoTime();
			channel.connect();
			
			byte[] tmp=new byte[1024];
			while(true){
			    while(true){
			    	int i=in.read(tmp, 0, 1024);
			    	if(i<0) {
			    		break;
			    	}
			    	outStr.write(tmp, 0, i);
			    }
			    if(channel.isClosed()){
			    	exitCode = channel.getExitStatus();
			    	break;
			    }
			}
			channel.disconnect();
			long endTime = System.nanoTime();

			String outText = new String(outStr.toByteArray());
			String errText = new String(errStr.toByteArray());
			
			return new ExecutionResult(outText, errText, exitCode, TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
			
		} catch (Exception e) {
			logger.error("SSH fauilure " + e);
			logger.error("SSH command: " + command);
			logger.error("SSH stdout\n" + new String(outStr.toByteArray()));
			logger.error("SSH stderr\n" + new String(errStr.toByteArray()));
			throw new RuntimeException(e);
		}	
	}

}
