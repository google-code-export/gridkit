package org.gridkit.coherence.misc.pofviewer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PofFinePrinter {

	private String POF_PATH_REGEX = "(\\d+)[:](\\d+(.\\d+)*)";
	private Pattern POF_PATH_PATTERN = Pattern.compile(POF_PATH_REGEX);
	
	private Map<Integer, String> typeNames = new HashMap<Integer, String>();
	private Map<Integer, Map<PofPath, Rule>> rules = new HashMap<Integer, Map<PofPath, Rule>>();
	
	public void loadConfiguration(Properties props) {
		for(Object k: props.keySet()) {
			String key = (String) k;
			String val = props.getProperty(key);
			
			String typeName = null;
			String attrName = null;
			if (key.indexOf('.') >= 0) {
				typeName = key.substring(0, key.lastIndexOf('.'));
				attrName = key.substring(typeName.length() + 1);
			}
			else {
				attrName = key;
			}
			
			Matcher m = POF_PATH_PATTERN.matcher(val);
			if (m.matches()) {
				int typeId = Integer.parseInt(m.group(1));
				String[] indexes = m.group(2).split(".");
				PofPath p = PofPath.root();
				for(String i: indexes) {
					p = p.a(Integer.parseInt(i));
				}
				addRule(new Rule(typeId, p, typeName, attrName));
			}
			else {
				throw new IllegalArgumentException();
			}
		}
	}
	
	public String getClassName(String typeId) {
		return typeNames.get(typeId);
	}
	
	public Map<PofPath, String> findAliases(List<PofEntry> entries) {		
		Map<PofPath, String> result = new HashMap<PofPath, String>();
		result.put(PofPath.root(), "");
		
		TreeMap<PofPath, String> proposals = new TreeMap<PofPath, String>();
		
		for(PofEntry entry: entries) {
			proposals.headMap(entry.getPath()).clear();

			String name = proposals.get(entry.getPath());
			if (name != null) {
				result.put(entry.getPath(), name);
			}
			
			entry.getTypeId();
			if (rules.containsKey(entry.getTypeId())) {
				String base = result.get(entry.getPath());
				if (base == null) {
					base = entry.getPath().toString();
				}
				if (base.length() > 0) {
					base += ".";
				}
				addProposals(proposals, entry.getPath(), base, rules.get(entry.getTypeId()));
			}
		}
		
		return result;
	}
	
	private void addProposals(TreeMap<PofPath, String> proposals, PofPath path, String base, Map<PofPath, Rule> typeRules) {
		for(Rule rule: typeRules.values()) {
			String name = base + rule.alias;
			proposals.put(path.append(rule.path), name);
		}
	}

	private void addRule(Rule rule) {
		Map<PofPath, Rule> type = rules.get(rule.userType);
		if (type == null) {
			type = new HashMap<PofPath, PofFinePrinter.Rule>();
			rules.put(rule.userType, type);
		}
		type.put(rule.path, rule);
		if (rule.className != null && rule.className.length() > 0) {
			typeNames.put(rule.userType, rule.className);
		}
	}
	
	private static class Rule {
		int userType;
		PofPath path;
		String className; 
		String alias;
		
		public Rule(int userType, PofPath path, String className, String alias) {
			this.userType = userType;
			this.path = path;
			this.className = className;
			this.alias = alias;
		}
	}
}
