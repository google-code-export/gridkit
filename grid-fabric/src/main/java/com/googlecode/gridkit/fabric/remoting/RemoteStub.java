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
package com.googlecode.gridkit.fabric.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RemoteStub implements InvocationHandler  {

	private RemoteInstance identity;
	private RmiChannel channel;
	
	public RemoteStub(RemoteInstance identity, RmiChannel channel) {
		this.identity = identity;
		this.channel = channel;
	}
	
	public RemoteInstance getRemoteInstance() {
		return identity;
	}

	public RmiChannel getRmiChannel() {
		return channel;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return channel.remoteInvocation(this, proxy, method, args);
	}
	
	public static Object buildProxy(RemoteInstance remoteInstance, RmiChannel channel) throws ClassNotFoundException {
		String[] classNames = remoteInstance.interfaces;
		Class[] classes = new Class[classNames.length];
		for(int i = 0; i != classNames.length; ++i) {
			classes[i] = channel.classForName(classNames[i]);
		}
		
		return Proxy.newProxyInstance(channel.getClassLoader(), classes, new RemoteStub(remoteInstance, channel));
	}
}
