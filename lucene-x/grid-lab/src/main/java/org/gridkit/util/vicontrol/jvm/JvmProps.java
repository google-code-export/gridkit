package org.gridkit.util.vicontrol.jvm;

public class JvmProps {

	/**
	 * Read-only process id of JVM
	 */
	public static String PROC_ID = "jvm:proc-id";
	
	/**
	 * Main classpath entry of JVM
	 */
	public static String CP = "jvm:cp"; 

	/**
	 * JVM classpath extension. Multiple named extensions could be specified.
	 */	
	public static String CP_EX = "jvm:cp:"; 

	/**
	 * Addition command line options for JVM.
	 */	
	public static String JVM_XX = "jvm:xx:"; 
	
}
