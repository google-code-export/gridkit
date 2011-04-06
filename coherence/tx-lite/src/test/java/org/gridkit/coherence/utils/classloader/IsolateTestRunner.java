/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
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
package org.gridkit.coherence.utils.classloader;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 *	@author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class IsolateTestRunner extends BlockJUnit4ClassRunner {

	private ClassLoader cl;
	
	public IsolateTestRunner(Class<?> klass) throws InitializationError, ClassNotFoundException {
		super(reload(klass));
		cl = getTestClass().getJavaClass().getClassLoader();
	}
	
	private static Class<?> reload(Class<?> klass) throws ClassNotFoundException {
		String name = klass.getName();
		Isolate i = new Isolate("ClassTest:" + name, "com.tangosol", "org.gridkit");
		return i.loadClass(name);
	}

	@Override
	public void run(RunNotifier notifier) {
		ClassLoader contextCl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		try {
			super.run(notifier);
		}
		finally {			
			Thread.currentThread().setContextClassLoader(contextCl);
		}
	}
}
