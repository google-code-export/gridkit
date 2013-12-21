package org.gridkit.nanocloud.tunneller;

import static org.gridkit.vicluster.ViX.CLASSPATH;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.SimpleCloudFactory;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViProps;
import org.gridkit.vicluster.telecontrol.ssh.RemoteNodeProps;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

// For manual runs on test zoo 
public class TunnelerProtocol_RemoteRun {

	public static Cloud cloud = SimpleCloudFactory.createSimpleSshCloud();
	public static ViNode host;
	
	@BeforeClass
	public static void initHost() {
		host = cloud.node("host");
		ViProps.at(host).setRemoteType();
		RemoteNodeProps.at(host).setRemoteHost("cbox1");
		host.x(CLASSPATH).add("../vicluster-core/target/test-classes");
	}
	
	@AfterClass
	public static void dropCloud() {
		cloud.shutdown();
	}
	
	@Test
	public void ping() {
		host.exec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Ping");				
			}
		});
	}

	@Test
	public void test_vanila_exec() {
		runTest("test_vanila_exec");
	}

	@Test
	public void test_exec_with_stdErr() {
		runTest("test_exec_with_stdErr");
	}
	
	@Test
	public void test_exec_with_redirect() {
		runTest("test_exec_with_redirect");
	}
	
	@Test
	public void test_exec_resource_leak() {
		runTest("test_exec_resource_leak");
	}
	
	@Test
	public void test_inherited_environment() {
		runTest("test_inherited_environment");
	}
	
	@Test
	public void test_inherited_environment_empty_map() {
		runTest("test_inherited_environment_empty_map");
	}
	
	@Test
	public void test_override_path() {
		runTest("test_override_path");
	}
	
	@Test
	public void test_set_env_var() {
		runTest("test_set_env_var");
	}
	
	@Test
	public void  test_env_var_override(){
		runTest("test_env_var_override");
	}
	
	@Test
	public void test_env_var_remove() {
		runTest("test_env_var_remove");
	}
	
	// see original method
	public void test_bind() {
		runTest("test_bind");
	}
	
	@Test
	public void test_vanila_file_push() {
		runTest("test_vanila_file_push");
	}
	
	@Test
	public void test_user_home_file_push() {
		runTest("test_user_home_file_push");
	}
	
	@Test
	public void test_temp_dir_file_push() {
		runTest("test_temp_dir_file_push");
	}
	
	@Test
	public void test_mkdirs_on_file_push() {
		runTest("test_mkdirs_on_file_push");
	}
		
	@Test
	public void test_no_override() {
		runTest("test_no_override");
	}
	
	@Test
	public void test_error_handling() {
		runTest("test_error_handling");
	}
	
	@Test
	public void test_error_handling2() {
		runTest("test_error_handling2");
	}
	
	@Test
	public void test_error_handling3() {
		runTest("test_error_handling3");
	}
	
	@Test
	public void test_concurrent_write_handling() {
		runTest("test_concurrent_write_handling");
	}
		
	private void runTest(final String method) {
		host.exec(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					Class<?> testClass = Class.forName("org.gridkit.vicluster.telecontrol.TunnellerProtocolTest");
					Object test = testClass.newInstance();
					testClass.getMethod("start").invoke(test);
					
					Method m = test.getClass().getMethod(method);
					m.invoke(test);
				} catch (SecurityException e) {
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					if (e.getCause() instanceof Error) {
						throw (Error)e.getCause();
					}
					else {
						throw new RuntimeException(e.getCause());
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	@AfterClass
	public static void shutdownHost() {
		cloud.shutdown();
	}
	
}
