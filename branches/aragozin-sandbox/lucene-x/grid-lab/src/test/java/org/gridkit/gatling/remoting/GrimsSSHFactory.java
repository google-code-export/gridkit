package org.gridkit.gatling.remoting;

import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class GrimsSSHFactory implements SSHFactory {

	private String user = "grimisuat";
	private String password = "@Mon_day5";
	
	@Override
	public Session getSession(final String host) throws JSchException {
		UserInfo ui = new UserInfo() {
			@Override
			public void showMessage(String message) {
				System.out.println("[" + host + "] SSH: " + message);
			}
			
			@Override
			public boolean promptYesNo(String message) {
				System.out.println("[" + host + "] SSH: " + message + " <- yes");
				return true;
			}
			
			@Override
			public boolean promptPassword(String message) {
				System.out.println("[" + host + "] SSH: " + message + " <- yes");
				return true;
			}
			
			@Override
			public boolean promptPassphrase(String message) {
				System.out.println("[" + host + "] SSH: " + message + " <- yes");
				return true;
			}
			
			@Override
			public String getPassword() {
				System.out.println("[" + host + "] SSH: password = " + password);
				return password;
			}
			
			@Override
			public String getPassphrase() {
				System.out.println("[" + host + "] SSH: passphrase = " + password);
				return password;
			}
		};
		
		JSch jsc = new JSch();
		jsc.addIdentity("C:/fire_at_will/grimis_id.dsa");
		Session session = jsc.getSession(user, host);
		session.setDaemonThread(true);
		session.setUserInfo(ui);
		session.setPassword(password);
		session.connect();
		
		return session;
	}

	static {
		JSch.setLogger(new JSchLogger());
	}
	
	private final static class JSchLogger implements Logger {
		
		private org.slf4j.Logger logger;
		
		public JSchLogger() {
			logger = LoggerFactory.getLogger("remoting.ssh.jsch");
		}
		
		@Override
		public void log(int level, String message) {
			switch(level) {
				case DEBUG: 
					logger.debug(message);
					break;
				case WARN: 
					logger.warn(message);
					break;
				case INFO: 
					logger.info(message);
					break;
				case ERROR: 
					logger.error(message);
					break;
				case FATAL: 
					logger.error(message);
					break;
				default:
					logger.warn(message);
			}			
		}
	
		@Override
		public boolean isEnabled(int level) {
			switch(level) {
			case DEBUG: 
				return logger.isDebugEnabled();
			case WARN: 
				return logger.isWarnEnabled();
			case INFO: 
				return logger.isInfoEnabled();
			case ERROR: 
				return logger.isErrorEnabled();
			case FATAL: 
				return logger.isErrorEnabled();
			default: 
				return logger.isWarnEnabled();
			}
		}
	}	
}
