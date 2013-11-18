package org.gridkit.lab.data.jorka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.TreeMap;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;

@SuppressWarnings("unused")
public class Jorka extends Object {

	// manage string like %{Foo} => Foo
	private static java.util.regex.Pattern PATTERN = java.util.regex.Pattern
			.compile("%\\{(.*?)\\}");
	private static Pattern PATTERN_RE = Pattern.compile("%\\{" + "(?<name>"
			+ "(?<pattern>[A-z0-9]+)" + "(?::(?<subname>[A-z0-9_:]+))?" + ")"
			+ "(?:=(?<definition>" + "(?:" + "(?:[^{}]+|\\.+)+" + ")+" + ")"
			+ ")?" + "\\}");

	Map<String, String> patterns;
	String savedPattern = null;

	// Private
	private Map<String, String> capturedMap;

	private String expandedPattern;
	private String patternOrigin;
	private Pattern regexp;
	private Discovery disco;

	/**
	 ** Constructor.
	 **/
	public Jorka() {

		patternOrigin = null;
		disco = null;
		expandedPattern = null;
		regexp = null;
		patterns = new TreeMap<String, String>();
		capturedMap = new TreeMap<String, String>();
	}

	/**
	 * Add a new pattern
	 * 
	 * @param name
	 *            Name of the pattern
	 * @param pattern
	 *            regex string
	 **/
	public void addPattern(String name, String pattern) {
		patterns.put(name, pattern);
	}

	/**
	 * Add patterns to {@link Jorka}
	 * 
	 * @param Map
	 *            of the pattern to copy
	 **/
	public void addPatterns(Map<String, String> cpy) {
		for (Map.Entry<String, String> entry : cpy.entrySet()) {
			patterns.put(entry.getKey().toString(), entry.getValue().toString());
		}
	}

	/**
	 * @return currently added patterns
	 */
	public Map<String, String> getPatterns() {
		return this.patterns;
	}

	/**
	 * @return the compiled regex of <tt>expanded_pattern</tt>
	 * @see compile
	 */
	public Pattern getRegEx() {
		return regexp;
	}

	/**
	 * 
	 * @return the string pattern
	 * @see compile
	 */
	public String getExpandedPattern() {
		return expandedPattern;
	}

	/**
	 * Add patterns from a file 
	 */
	public void addPatternFromFile(String file) throws IOException {

		File f = new File(file);
		addPatternFromReader(new FileReader(f));
	}

	/**
	 * Add patterns  from a reader
	 */
	public void addPatternFromReader(Reader r) throws IOException {
		BufferedReader br = new BufferedReader(r);
		String line;
		// We dont want \n and commented line
		Pattern MY_PATTERN = Pattern.compile("^([A-z0-9_]+)\\s+(.*)$");
		while ((line = br.readLine()) != null) {
			Matcher m = MY_PATTERN.matcher(line);
			if (m.matches())
				this.addPattern(m.group(1), m.group(2));
		}
		br.close();
	}

	/**
	 * Match the <tt>text</tt> with the pattern
	 * 
	 * @param text to match
	 * @see Match
	 */
	public Match match(String text) {

		if (regexp == null)
			return null;

		Matcher m = regexp.matcher(text);
		Match match = new Match();
		// System.out.println(expanded_pattern);
		if (m.find()) {
			// System.out.println("LLL"+m.group() +" " + m.start(0) +" "+
			// m.end(0));
			match.setSubject(text);
			match.jorka = this;
			match.match = m;
			match.start = m.start(0);
			match.end = m.end(0);
			match.line = text;
			return match;
		}
		return match;
	}

	/**
	 * Transform Jorka regex into a compiled regex
	 */
	public void compile(String pattern) {
		expandedPattern = new String(pattern);
		patternOrigin = new String(pattern);
		int index = 0;
		Boolean Continue = true;

		// Replace %{foo} with the regex (mostly groupname regex)
		// and then compile the regex
		while (Continue) {
			Continue = false;

			Matcher m = PATTERN_RE.matcher(expandedPattern);
			// Match %{Foo:bar} -> pattern name and subname
			// Match %{Foo=regex} -> add new regex definition
			if (m.find()) {
				Continue = true;
				Map<String, String> group = m.namedGroups();

				if (group.get("definition") != null) {
					addPattern(group.get("pattern"), group.get("definition"));
					group.put("name",
							group.get("name") + "=" + group.get("definition"));
				}
				capturedMap.put("name" + index,
						(group.get("subname") != null ? group.get("subname")
								: group.get("name")));
				expandedPattern = expandedPattern.replace((CharSequence) "%{"
						+ group.get("name") + "}", "(?<name" + index + ">"
						+ this.patterns.get(group.get("pattern")) + ")");
				index++;
			}
		}

		// Compile the regex
		if (!expandedPattern.isEmpty()) {
			regexp = Pattern.compile(expandedPattern);
		} else {
			throw new IllegalArgumentException("Pattern is not found '"
					+ pattern + "'");
		}
	}

	/**
	 * TODO
	 */
	public String discover(String input) {

		if (disco == null)
			disco = new Discovery(this);
		return disco.discover(input);
	}

	/**
	 * 
	 * @return getter
	 */
	public Map<String, String> getCaptured() {
		return capturedMap;
	}

	/**
	 ** Checkers
	 **/
	public int isPattern() {
		if (patterns == null)
			return 0;
		if (patterns.isEmpty())
			return 0;
		return 1;
	}
}
