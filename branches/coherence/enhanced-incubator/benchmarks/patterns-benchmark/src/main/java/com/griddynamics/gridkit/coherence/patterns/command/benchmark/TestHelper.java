/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
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
package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

public class TestHelper {

	public static void sysout(String text, Object... args) {
		System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", System.currentTimeMillis()) + String.format(text, args));
	}
	
	public static void setSysProp(String prop, String value) {
		if (System.getProperty(prop) == null) {
			System.setProperty(prop, value);
		}
		System.out.println("[SysProp] " + prop + ": " + System.getProperty(prop));
	}
}
