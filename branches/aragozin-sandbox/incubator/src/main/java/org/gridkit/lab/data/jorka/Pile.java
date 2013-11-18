package org.gridkit.lab.data.jorka;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Pile {

	// Private
	private List<Jorka> jorkas;
	private Map<String, String> patterns;
	private List<String> pattern_files;

	static final String defaultPatternDirectory = "patterns/";

	/**
	 ** Constructor
	 **/
	public Pile() {
		patterns = new TreeMap<String, String>();
		jorkas = new ArrayList<Jorka>();
		pattern_files = new ArrayList<String>();
	}

	/**
	 * 
	 * @param name
	 *            of the pattern
	 * @param file
	 *            path
	 * @return
	 */
	public void addPattern(String name, String file) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("name is empty");
		} else if (file.isEmpty()) {
			throw new IllegalArgumentException("file is empty");
		}
		patterns.put(name, file);
	}

	/**
	 * Load patterns file from a directory
	 * 
	 * @param directory
	 */
	public void addFromDirectory(String directory) {

		if (directory == null || directory.isEmpty()) {
			directory = defaultPatternDirectory;
		}

		File dir = new File(directory.toString());
		File lst[] = dir.listFiles();

		for (int i = 0; i < lst.length; i++)
			if (lst[i].isFile())
				addPatternFromFile(lst[i].getAbsolutePath());
	}

	/**
	 * Add pattern from a file
	 */
	public void addPatternFromFile(String file) {

		File f = new File(file);
		if (!f.exists()) {
			throw new IllegalArgumentException("No such file: " + file);
		}
		pattern_files.add(file);
	}

	/**
	 * Compile the pattern with a corresponding {@link Jorka}
	 * 
	 */
	public void compile(String pattern) throws IOException {

		Jorka jorka = new Jorka();

		Map<String, String> map = new TreeMap<String, String>();

		for (Map.Entry<String, String> entry : patterns.entrySet()) {
			if (!map.containsValue((entry.getValue()))) {
				jorka.addPattern(entry.getKey().toString(), entry.getValue()
						.toString());
			}
		}

		for (String file : pattern_files) {
			jorka.addPatternFromFile(file);
		}

		jorka.compile(pattern);
		jorkas.add(jorka);
	}

	public Match match(String line) {
		for (Jorka grok : jorkas) {
			Match gm = grok.match(line);
			if (gm != null) {
				return gm;
			}
		}

		return null;
	}
}
