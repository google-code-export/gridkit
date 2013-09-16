package org.gridkit.lab.ptml;

import org.junit.Test;
import org.mvel2.MVEL;

public class LambdaCheck {

	
	@Test
	public void lambda() {
		
		System.out.println(MVEL.eval("def (x) { x >= 10 ? x : 0 }"));
		
	}
	
}
