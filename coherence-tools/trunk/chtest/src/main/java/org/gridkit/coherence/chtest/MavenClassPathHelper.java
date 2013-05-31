package org.gridkit.coherence.chtest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


/**
 * Utility class to configure inclusion, exclusion and replacement
 * of maven dependencies in classpath.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class MavenClassPathHelper {

	private static Class<?> ANCHOR = MavenClassPathHelper.class;
	
	private static Map<String, SourceInfo> CLASSPATH_JARS;
	
	private static File LOCAL_MAVEN_REPOPATH;
	
	public static File getLocalMavenRepoPath() {
		initClasspath();
		findLocalMavenRepo();
		return LOCAL_MAVEN_REPOPATH;
	}
	
	public static String getArtifactVersion(String groupId, String artifactId) {
		String cppath = "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
		InputStream is = ANCHOR.getResourceAsStream(cppath);
		if (is != null) {
			try {
				Properties prop = new Properties();
				prop.load(is);
				is.close();
				return prop.getProperty("version");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			// artifact we are looking for does not provide Maven manifest, let's take hard way
			initClasspath();
			for(SourceInfo si: CLASSPATH_JARS.values()) {
				if (si.jarUrl != null) {
					String artBase = getMavenArtifactBase(si.jarUrl);
					if (artBase != null) {
						String path = "/" + groupId.replace('.', '/') + "/" + artifactId;
						if (artBase.endsWith(path)) {
							return getMavenVersionFromRepoPath(si.jarUrl);
						}
					}
				}
			}
			throw new IllegalArgumentException("Cannot detect version for " + groupId + ":" + artifactId);
		}
	}

	public static List<String> getAvailableVersions(String groupId, String artifactId) {
		File localRepo = getLocalMavenRepoPath();
		if (localRepo == null) {
			throw new IllegalArgumentException("Cannot detect local repo");
		}
		String[] gp = groupId.split("[.]");
		File path = localRepo;
		for(String p: gp) {
			path = new File(path, p);
		}
		path = new File(path, artifactId);
		List<String> version = new ArrayList<String>();
		for(File c: path.listFiles()) {
			if (c.isDirectory()) {
				if (findJar(c, artifactId) != null) {
					version.add(c.getName());
				}
			}
		}
		Collections.sort(version);
		return version;
	}
	
	private static String findJar(File c, String artifactId) {
		for(File f: c.listFiles()) {
			if (!f.isDirectory() && f.getName().startsWith(artifactId + "-") && f.getName().endsWith(".jar")) {
				return f.getName();
			}
		}
		return null;
	}

	public static URL getArtifactClasspathUrl(String groupId, String artifactId) {
		String cppath = "/META-INF/maven/" + groupId + "/" + artifactId;
		URL url = ANCHOR.getResource(cppath);
		if (url != null) {
			try {
				String us = url.toExternalForm();
				us = us.substring(0, us.length() - cppath.length());
				return new URL(us);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			// artifact we are looking for does not provide Maven manifest, let's take hard way
			initClasspath();
			for(SourceInfo si: CLASSPATH_JARS.values()) {
				if (si.jarUrl != null) {
					String artBase = getMavenArtifactBase(si.jarUrl);
					if (artBase != null) {
						String path = "/" + groupId.replace('.', '/') + "/" + artifactId;
						if (artBase.endsWith(path)) {
							try {
								return new URL(si.baseUrl);
							} catch (MalformedURLException e) {
								throw new RuntimeException(e);
							}
						}
					}
				}
			}
			throw new IllegalArgumentException("Cannot detect version for " + groupId + ":" + artifactId);
		}
	}

	public static URL findJar(String groupId, String artifactId, String version) {
		File localRepo = getLocalMavenRepoPath();
		if (localRepo == null) {
			throw new IllegalArgumentException("Cannot detect local repo");
		}
		String[] gp = groupId.split("[.]");
		File path = localRepo;
		for(String p: gp) {
			path = new File(path, p);
		}
		path = new File(path, artifactId);
		path = new File(path, version);
		String jarName = findJar(path, artifactId);
		if (jarName != null) {
			File jar = new File(path, jarName);
			try {
				return jar.toURI().toURL();
			} catch (MalformedURLException e) {
				return null;
			}
		}
		return null;
	}
	
	private static String getMavenArtifactBase(String path) {
		int c = path.lastIndexOf('/');
		if (c <= 0) {
			return null;
		}
		path = path.substring(0, c);
		c = path.lastIndexOf('/');
		if (c <= 0) {
			return null;
		}
		path = path.substring(0, c);
		return path;
	}

	private static String getMavenVersionFromRepoPath(String path) {
		int c = path.lastIndexOf('/');
		if (c <= 0) {
			return null;
		}
		path = path.substring(0, c);
		c = path.lastIndexOf('/');
		if (c <= 0) {
			return null;
		}
		path = path.substring(c + 1);
		return path;
	}
	
	private synchronized static void initClasspath() {
		if (CLASSPATH_JARS != null) {
			return;
		}
		Enumeration<URL> en;
		try {
			en = ANCHOR.getClassLoader().getResources("META-INF/MANIFEST.MF");
		} catch (IOException e) {
			throw new RuntimeException("Failed to scan classpath", e);
		}
		CLASSPATH_JARS = new HashMap<String, MavenClassPathHelper.SourceInfo>();
		while(en.hasMoreElements()) {
			URL url = en.nextElement();
			SourceInfo info;
			try {
				info = readInfo(url);
				CLASSPATH_JARS.put(info.baseUrl, info);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private synchronized static void findLocalMavenRepo() {
		initClasspath();
		if (LOCAL_MAVEN_REPOPATH != null) {
			return;
		}
		for(SourceInfo si:	CLASSPATH_JARS.values()) {
			try {
				if (si.mavenProps != null && si.jarUrl != null) {
					String group = si.mavenProps.getProperty("groupId");
					String artifact = si.mavenProps.getProperty("artifactId");
					String version = si.mavenProps.getProperty("version");
					String path = "/" + group.replace('.', '/') + "/" + artifact + "/" + version + "/" + artifact + "-" + version + ".jar";
					if (si.jarUrl.endsWith(path)) {
						String repo = si.jarUrl.substring(0, si.jarUrl.length() - path.length());
						URI ru = new URI(repo);
						LOCAL_MAVEN_REPOPATH = new File(ru);
						return;
					}
				}
			} catch (URISyntaxException e) {
				// ignore
			}
		}
	}
	
	private static SourceInfo readInfo(URL url) throws IOException {
		String upath = url.toString();
		upath = upath.substring(0, upath.length() - "META-INF/MANIFEST.MF".length());
		InputStream is = url.openStream();
		Manifest mf = new Manifest();
		mf.read(is);
		is.close();
		SourceInfo info = new SourceInfo();
		if (upath.startsWith("jar:")) {
			String jarPath = upath.substring("jar:".length());
			int c = jarPath.indexOf('!');
			jarPath = jarPath.substring(0, c);
			info.jarUrl = jarPath;
		}
		info.baseUrl = upath;
		info.manifest = mf;
		info.mavenProps = loadMavenProps(upath);
		return info;
	}

	private static Properties loadMavenProps(String upath) throws IOException {
		List<String> paths = listFiles(new ArrayList<String>(), new URL(upath + "META-INF/maven/"), "META-INF/maven/");
		Properties prop = null;
		for(String path: paths) {
			if (path.endsWith("/pom.properties")) {
				if (prop != null) {
					// ambiguous maven properties, ignoring
					return null;
				}
				URL url = new URL(upath + path);
				InputStream is = url.openStream();
				prop = new Properties();
				prop.load(is);
				is.close();
			}
		}
		return prop;
	}

	private static class SourceInfo {
		
		String baseUrl;
		String jarUrl;
		@SuppressWarnings("unused")
		Manifest manifest;
		Properties mavenProps;
		
	}	
	
	static List<String> findFiles(String path) throws IOException {
		List<String> result = new ArrayList<String>();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> en = cl.getResources(path);
		while(en.hasMoreElements()) {
			URL u = en.nextElement();
			listFiles(result, u, path);
		}
		return result;
	}
	
	static List<String> listFiles(List<String> results, URL packageURL, String path) throws IOException {

	    if(packageURL.getProtocol().equals("jar")){
	        String jarFileName;
	        JarFile jf ;
	        Enumeration<JarEntry> jarEntries;
	        String entryName;

	        // build jar file name, then loop through zipped entries
	        jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
	        jarFileName = jarFileName.substring(5,jarFileName.indexOf("!"));
	        jf = new JarFile(jarFileName);
	        jarEntries = jf.entries();
	        while(jarEntries.hasMoreElements()){
	            entryName = jarEntries.nextElement().getName();
	            if(entryName.startsWith(path)){
	                results.add(entryName);
	            }
	        }

	    // loop through files in classpath
	    }else{
	        File dir = new File(packageURL.getFile());
	        String cp = dir.getCanonicalPath();
	        File root = dir;
	        while(true) {
	        	if (cp.equals(new File(root, path).getCanonicalPath())) {
	        		break;
	        	}
	        	root = root.getParentFile();
	        }
	        listFiles(results, root, dir);
	    }
	    return results;
	}

	static void listFiles(List<String> names, File root, File dir) {
		String rootPath = root.getAbsolutePath(); 
		if (dir.exists() && dir.isDirectory()) {
			String dname = dir.getAbsolutePath().substring(rootPath.length() + 1);
			dname = dname.replace('\\', '/');
			names.add(dname);
			for(File file: dir.listFiles()) {
				if (file.isDirectory()) {
					listFiles(names, root, file);
				}
				else {
					String name = file.getAbsolutePath().substring(rootPath.length() + 1);
					name = name.replace('\\', '/');
					names.add(name);
				}
			}
		}
	}	
}
