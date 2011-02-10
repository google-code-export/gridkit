package org.apteka.insurance.dictionary.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.output.FileWriterWithEncoding;

public class DictionaryGenerator {
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		String targetFolder = args[0];
		String packagePrefix = args[1];
		String targetFile = args[2];
		int startId = Integer.valueOf(args[3]);

		Collection<File> files = listClasses(targetFolder);
		
		Collection<String> classes = getClasses(files, packagePrefix);
		
		ClassLoader classLoader = new URLClassLoader(getFileURLs(targetFolder), DictionaryGenerator.class.getClassLoader());
		
		List<DictionaryEntry> entries = new ArrayList<DictionaryEntry>();
		for (String clazz : classes) {
			int initSize = entries.size();
			entries.addAll(DictionaryEntryUtil.describe(classLoader.loadClass(clazz), startId, packagePrefix));
			startId += entries.size() - initSize;
		}
		
		writeDictionary(targetFile, entries);
	}
	
	private static void writeDictionary(String targetFile, List<DictionaryEntry> entries) throws IOException {
		FileWriterWithEncoding fstream = new FileWriterWithEncoding(targetFile, "UTF-8");
		BufferedWriter out = new BufferedWriter(fstream);
			
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		out.write("<attributes>\n");
		
		for (DictionaryEntry entry : entries) {
			out.write(DictionaryEntryUtil.toXML(entry));
		}
		
		out.write("</attributes>\n");
			
		out.close();
	}
	
	private static Collection<String> getClasses(Collection<File> files, String packagePrefix) throws MalformedURLException {
		Set<String> result = new HashSet<String>();
		
		for (File file : files) {
			String template = file.toURI().toURL().toString().replace('/', '.');
			int index = template.indexOf(packagePrefix);
			if (index > 0)
				result.add(template.substring(index, template.length() - ".class".length()));
		}
		
		return result;
	}
	
	private static Collection<File> listClasses(String directory) {
		return FileUtils.listFiles(new File(directory), new SuffixFileFilter(".class"), TrueFileFilter.TRUE);
	}
	
	private static URL[] getFileURLs(String... files) throws MalformedURLException{
		URL[] result = new URL[files.length];
		
		int i = 0;
		for (String file : files)
			result[i++] = (new File(file)).toURI().toURL();
		
		return result;
	}
}

//String targetFolder = "target/classes";
//String packagePrefix = "org.apteka.insurance.dictionary";
//String targetFile = "target/dictionary.xml";
//int startId = 0;
