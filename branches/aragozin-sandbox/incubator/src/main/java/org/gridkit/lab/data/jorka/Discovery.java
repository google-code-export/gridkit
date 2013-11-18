package org.gridkit.lab.data.jorka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;

public class Discovery {

	private Jorka jorka;

	/**
	 ** Constructor
	 **/
	public Discovery(Jorka jorka) {
		this.jorka = jorka;
	}

	/**
	 * Sort by regex complexity
	 * 
	 * @param Map of the pattern name and jorka instance
	 * @return the map sorted by pattern complexity
	 */
	private Map<String, Jorka> sort(Map<String, Jorka> jorkas) {

		List<Jorka> jorky = new ArrayList<Jorka>(jorkas.values());
		Map<String, Jorka> jorka = new LinkedHashMap<String, Jorka>();
		Collections.sort(jorky, new Comparator<Jorka>() {

			public int compare(Jorka g1, Jorka g2) {
				return (this.complexity(g1.getExpandedPattern()) < this
						.complexity(g2.getExpandedPattern())) ? 1 : 0;
			}

			private int complexity(String expandedPattern) {
				int score = 0;
				score += expandedPattern.split("\\Q" + "|" + "\\E", -1).length - 1;
				score += expandedPattern.length();
				return score;
			}
		});

		for (Jorka j : jorky) {
			jorka.put(j.savedPattern, j);
		}
		return jorka;

	}

	/**
	 * 
	 * @param expandedPattern string
	 * @return the complexity of the regex
	 */
	private int complexity(String expandedPattern) {
		int score = 0;

		score += expandedPattern.split("\\Q" + "|" + "\\E", -1).length - 1;
		score += expandedPattern.length();

		return score;
	}

	/**
	 * TODO 
	 */
	public String discover(String text) {
		if (text == null)
			return "";

		Map<String, Jorka> groks = new TreeMap<String, Jorka>();
		Map<String, String> gPatterns = jorka.getPatterns();
		// Boolean done = false;
		String texte = new String(text);

		// Compile the pattern
		Iterator<Entry<String, String>> it = gPatterns.entrySet().iterator();
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry) it.next();
			String key = pairs.getKey().toString();
			Jorka g = new Jorka();

			// g.patterns.putAll( gPatterns );
			g.addPatterns(gPatterns);
			g.savedPattern = key;
			g.compile("%{" + key + "}");
			groks.put(key, g);
		}

		// Sort patterns by complexity
		Map<String, Jorka> patterns = this.sort(groks);

		// while (!done){
		// done = true;
		Iterator<Entry<String, Jorka>> pit = patterns.entrySet().iterator();
		while (pit.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry) pit.next();
			String key = pairs.getKey().toString();
			Jorka value = (Jorka) pairs.getValue();

			// We want to search with more complex pattern
			// We avoid word, small number, space....
			if (this.complexity(value.getExpandedPattern()) < 20)
				continue;

			Match m = value.match(text);
			if (m.isNull())
				continue;
			// get the part of the matched text
			String part = getPart(m, text);

			// we skip boundary word
			Pattern MY_PATTERN = Pattern.compile(".\\b.");
			Matcher ma = MY_PATTERN.matcher(part);
			if (!ma.find())
				continue;

			// We skip the part that already include %{Foo}
			Pattern MY_PATTERN2 = Pattern.compile("%\\{[^}+]\\}");
			Matcher ma2 = MY_PATTERN2.matcher(part);

			if (ma2.find())
				continue;
			texte = texte.replace((CharSequence) part, "%{" + key + "}");
		}
		// }

		return texte;
	}

	/**
	 * Get the substring tht match with the text
	 * 
	 * @return string
	 */
	private String getPart(Match m, String text) {

		if (m == null || text == null)
			return "";

		return text.substring(m.start, m.end);
	}
}
