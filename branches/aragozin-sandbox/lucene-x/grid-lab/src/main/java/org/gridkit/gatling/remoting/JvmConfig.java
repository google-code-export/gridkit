package org.gridkit.gatling.remoting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JvmConfig implements Serializable {

	private static final long serialVersionUID = 20120211L;
	
	private String jdk = null;
	private List<String> jvmOptions = new ArrayList<String>();
	private List<String> classpathExtras = new ArrayList<String>();
	
	public JvmConfig() {		
	}
	
}
