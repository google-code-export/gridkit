/*
 * File: CanceledFunctor.java
 * 
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.
 * 
 * Oracle is a registered trademark of Oracle Corporation and/or its
 * affiliates.
 * 
 * This software is the confidential and proprietary information of Oracle
 * Corporation. You shall not disclose such confidential and proprietary
 * information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Oracle Corporation.
 * 
 * Oracle Corporation makes no representations or warranties about 
 * the suitability of the software, either express or implied, 
 * including but not limited to the implied warranties of 
 * merchantability, fitness for a particular purpose, or 
 * non-infringement.  Oracle Corporation shall not be liable for 
 * any damages suffered by licensee as a result of using, modifying 
 * or distributing this software or its derivatives.
 * 
 * This notice may not be removed or altered.
 */

import java.io.IOException;
import java.io.Serializable;

import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.oracle.coherence.patterns.functor.Functor;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;


public class CanceledFunctor implements Functor<TestContext, Boolean>, Serializable, PortableObject {

	private static final long serialVersionUID = 7664994823154953101L;	
	
	public CanceledFunctor() {
	}
	
	public void readExternal(PofReader reader) throws IOException {
	}

	public void writeExternal(PofWriter writer) throws IOException {
	}

	public Boolean execute(
			ExecutionEnvironment<TestContext> executionEnvironment) {
		return true;
	}
}
