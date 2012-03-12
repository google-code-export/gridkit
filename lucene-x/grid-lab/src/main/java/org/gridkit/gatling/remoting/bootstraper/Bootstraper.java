package org.gridkit.gatling.remoting.bootstraper;

public class Bootstraper {

	private String id;
	private String home;
	private int port; 
	
	private BootstrapClassloader classloader;
	
	public Bootstraper(String id, int port, String home) {
		this.id = id;
		this.port = port;
		this.home = home;
		this.classloader = new BootstrapClassloader(Thread.currentThread().getContextClassLoader());
	}

	public void run() {
		
	}
	
	public void Bootstraper(String[] args) {
		String id = args[0];
		int port = Integer.valueOf(args[1]);
		String home = args[2];
		
		new Bootstraper(id, port, home).run();
	}	
	
	private class BootstrapClassloader extends ClassLoader {
		public BootstrapClassloader(ClassLoader parent) {
			super(parent);
		}
	}
}
