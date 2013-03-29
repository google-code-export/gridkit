/**
 * Copyright 2013 Alexey Ragozin
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
package org.gridkit.coherence.chtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JarManager {

	private static Map<String, String> JAR_CACHE = new HashMap<String, String>();
	
	public static List<String> getAllAvailableVersions() {
		try {
			Enumeration<URL> en = JarManager.class.getClassLoader().getResources("coherence.jar.version");
			List<String> versions = new ArrayList<String>();
			while(en.hasMoreElements()) {
				URL url = en.nextElement();
				InputStream is = url.openStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				versions.add(br.readLine());
				br.close();
			}
			
			return versions;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getCoherenceJarPath() {
		String cfpath = "com/tangosol/net/CacheFactory.class";
		URL u = JarManager.class.getClassLoader().getResource(cfpath);
		if (u != null) { 
			String path = u.toString();
			if (path.startsWith("jar:")) {
				path = path.substring("jar:".length());
				path = path.substring(0, path.indexOf('!'));
				return path;
			}
			else {
				path = path.substring(0, path.length() - cfpath.length());
				return path;
			}
		}
		else {
			return null;
		}
	}
	
	public static synchronized String getJarPath(String version) {
		try {
			String path = JAR_CACHE.get(version);			
			if (path == null) {
				// First, try find jar assuming maven repository layout
				String curPath = new URI(getCoherenceJarPath()).getPath();
				if (curPath != null) {
					File newPath = new File(curPath).getParentFile().getParentFile();
					newPath = new File(newPath, version);
					newPath = new File(newPath, "coherence-" + version + ".jar");
					if (newPath.exists()) {
						path = newPath.getCanonicalPath();
						JAR_CACHE.put(version, path);
						return path;
					}
				}
				
				// Second, create jar from resources
				InputStream is = JarManager.class.getClassLoader().getResourceAsStream("coherence-" + version + ".jar");
				if (is == null) {
					throw new IllegalArgumentException("File coherence-" + version + ".jar is not found in classpath.\nFor JarManager to work you should add org.gridkit.jar-pack.coherence:" + version + ":1.0 dependecy to you project's pom");
				}
				File f = File.createTempFile("coherence-" + version + "-", ".jar");
				f.deleteOnExit();
				FileOutputStream fos = new FileOutputStream(f);
				while(true) {
					int n = is.available();
					n = n > 4 << 10 ? n : 4 << 10;
					byte[] buf = new byte[n];
					n = is.read(buf);
					if (n < 0) {
						break;
					}
					else {
						fos.write(buf, 0, n);
					}
				}
				fos.close();
				is.close();
				path = f.getCanonicalPath();
				JAR_CACHE.put(version, path);
			}
			return path;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
}
