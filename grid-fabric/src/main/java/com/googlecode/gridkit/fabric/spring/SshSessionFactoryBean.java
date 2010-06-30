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
package com.googlecode.gridkit.fabric.spring;

import java.util.logging.Logger;

import com.googlecode.gridkit.fabric.exec.ssh.SshExecutor;
import com.googlecode.gridkit.fabric.exec.ssh.SshSessionFactory;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SshSessionFactoryBean implements SshSessionFactory {

	private final Logger logger = Logger.getLogger(SshExecutor.class.getName());
	
	private String login;
	private String password;
	private String remoteHome;
	
	private final UserInfo userInfo = new UserInfo() {

		public String getPassphrase() {
			return password;
		}

		public String getPassword() {
			return password;
		}

		public boolean promptPassphrase(String message) {
			logger.fine("SSH:promptPassphrase: " + message);
			return password != null;
		}

		public boolean promptPassword(String message) {
			logger.fine("SSH:promptPassword: " + message);
			return password != null;
		}

		public boolean promptYesNo(String message) {
			logger.fine("SSH:promptYesNo: " + message);
			return true;
		}

		public void showMessage(String message) {
			logger.fine("SSH:showMessage: " + message);
		}
	};
	
	private JSch jsch = new JSch();
	
	public void setLogin(String login) {
		this.login = login;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setKeyFile(String keyFile) {
		try {
			jsch.addIdentity(keyFile);
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
	}

	public Session connect(String host) throws JSchException {
		Session session = jsch.getSession(login, host);
		session.setUserInfo(userInfo);
		session.connect();
		
		return session;
	}
}
