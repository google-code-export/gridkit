package org.gridkit.lab.zktest;

import java.io.File;
import java.io.IOException;

public class FSHelper {

	public static void removeDir(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					removeDir(file);
				} else {
					file.delete();
				}
			}
		}
		dir.delete();
	}		
	
	/**
	 * Transforms and conanize path according to local system.
	 * <li>
	 * Expands ~/
	 * </li>
	 * <li>
	 * Replaces {tmp} to local IO temp dir
	 * </li> 
	 */
	public static String normalizePath(String path) throws IOException {
		if (path.startsWith("~/")) {
			String home = System.getProperty("user.home");
			File fp = new File(new File(home), path.substring("~/".length()));
			return fp.getCanonicalPath();
		}
		else if (path.startsWith("{tmp}/")) {
			File tmp = File.createTempFile("mark", "").getAbsoluteFile();
			tmp.delete();
			File fp = new File(tmp.getParentFile(), path.substring("{tmp}/".length()));
			return fp.getCanonicalPath();
		}
		else {
			return new File(path).getCanonicalPath();
		}
	}		
}
