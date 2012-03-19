package org.gridkit.gatling.remoting.bootstraper;

public class Bootstraper {

	private String id;
	private int port; 
	
	public Bootstraper(String id, int port, String home) {
		this.id = id;
		this.port = port;
	}

	public void start() {
		
	}
	
	public void Bootstraper(String[] args) {
		String id = args[0];
		int port = Integer.valueOf(args[1]);
		String home = args[2];
		
		new Bootstraper(id, port, home).start();
	}	
}
