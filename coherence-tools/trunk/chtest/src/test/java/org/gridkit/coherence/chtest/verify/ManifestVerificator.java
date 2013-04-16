package org.gridkit.coherence.chtest.verify;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Assert;
import org.junit.Test;

import com.tangosol.coherence.component.application.console.Coherence;

public class ManifestVerificator {

	private DisposableCohCloud cloud = new DisposableCohCloud();

	@Test
	public void read_manifest() {

		cloud.node("node").exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				readManifest();
				Assert.assertFalse("n/a".equals(Coherence.getBuildNumber()));
				return null;
			}
		});
	}

	private static void readManifest() {
		String MANIFEST = "META-INF/MANIFEST.MF";
		CodeSource codeSource = null;
		InputStream in = null;
		Attributes attrs = null;
		try {
			ProtectionDomain pDomain = Coherence.class
					.getProtectionDomain();
			codeSource = pDomain != null ? pDomain.getCodeSource() : null;
			URL urlSrc = (codeSource != null ? codeSource.getLocation() : null);
			URL manifestUrl = null;
			
			if (urlSrc != null) {
				try {
					manifestUrl = new URL("jar:" + urlSrc + '!' + '/' + MANIFEST);
					URLConnection urlCon = manifestUrl.openConnection();
					urlCon.setUseCaches(false);
					in = urlCon.getInputStream();
				} catch (Exception e) {
				}
				if (in == null) {
					try {
						manifestUrl = new URL(urlSrc + new StringBuffer(String.valueOf('/')).append(	MANIFEST).toString());
						in = manifestUrl.openStream();
					} catch (Exception e) {						
					}
				}
				if (in != null) {
					
					try {
						new Manifest(in).getMainAttributes();
						System.out.println(manifestUrl + " - OK");
					} catch (Exception e) {
						System.err.println("Failed to parse manifest: " + manifestUrl);
						e.printStackTrace();
						BufferedReader reader = new BufferedReader(new InputStreamReader(manifestUrl.openStream()));
						while(true) {
							String line = reader.readLine();
							if (line == null) {
								break;
							}
							else {
								System.err.println("MF> " + line);
							}
						}
					}

					if ((attrs != null ? 0 : 1) != 0) {
						in.close();
					}

				}

			}

			if ((attrs != null ? 0 : 1) != 0) {
				ClassLoader loader = Coherence.class.getClassLoader();
				Enumeration<?> enumMF = loader == null 
							? ClassLoader.getSystemResources(MANIFEST) 
							: loader.getResources(MANIFEST);

				while (enumMF.hasMoreElements()) {
					manifestUrl = (URL) enumMF.nextElement();
					in = manifestUrl.openStream();
					try {
						new Manifest(in).getMainAttributes();
						System.out.println(manifestUrl + " - OK");
					} catch (Exception e) {
						System.err.println("Failed to parse manifest: " + manifestUrl);
						e.printStackTrace();
					}

					in.close();
				}
			}

		} catch (Throwable e) {
			e.printStackTrace(System.err);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
	}
}
