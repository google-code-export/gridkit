package org.apteka.insurance.dictionary.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apteka.insurance.dictionary.generator.util.ClassUtil;
import org.apteka.insurance.dictionary.generator.util.XmlUtil;

public class XmlDictionaryGenerator {
	public static void main(String[] args) throws ClassNotFoundException, IOException, ValidityException, ParsingException {
		String targetFolder = args[0];
		String packagePrefix = args[1];
		String targetFile = args[2];

		//String targetFolder = "../model/target/classes";
		//String packagePrefix = "org.apteka.insurance.model";
		//String targetFile = "target/dictionary.xml";
		
		Collection<File> files = listClasses(targetFolder);
		
		Collection<String> classes = getClasses(files, packagePrefix);
		
		ClassLoader classLoader = new URLClassLoader(getFileURLs(targetFolder), XmlDictionaryGenerator.class.getClassLoader());
		
		List<DictionaryEntry> entries = new ArrayList<DictionaryEntry>();
		for (String clazz : classes)
			entries.addAll(ClassUtil.describe(classLoader.loadClass(clazz), packagePrefix));
		
		Builder parser = new Builder();
		Document dictionary = parser.build(new File(targetFile));

		XmlUtil.populateDocument(dictionary, entries);
		
		FileOutputStream targetOutput = new FileOutputStream(new File(targetFile));
		
		Serializer serializer = new Serializer(targetOutput, "UTF-8");
		serializer.setIndent(4);
		serializer.write(dictionary);
		
		targetOutput.close();
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
