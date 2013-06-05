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

/**
 * This listener can be used to monitor a ConnectionHandler.
 * (ie. to know when it finishes)
 * 
 * @date   07/10/2006 
 * @author lipe

 * @see	   com.griddynamics.convergence.demo.utils.rmi.ConnectionHandler
 */

public interface IConnectionHandlerListener {

	void connectionClosed();
	
}