package org.gridkit.coherence.misc.pofviewer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tangosol.io.pof.PofConstants;

public class PofFinePrinter {

	private static Map<Integer, String> BUILD_IN_POF_TYPES = new HashMap<Integer, String>();
	static {
		Class<?> pc = PofConstants.class;
		for(Field f : pc.getFields()) {
			if (Modifier.isStatic(f.getModifiers()) && (f.getName().startsWith("T_") || f.getName().startsWith("V_")) && (f.getType() == Integer.TYPE)) {
				try {
					f.setAccessible(true);
					int id = f.getInt(null);
					BUILD_IN_POF_TYPES.put(id, f.getName().substring(2));
				} catch (Exception e) {
					// ignore
				}
			}
		}
		
		BUILD_IN_POF_TYPES.put(PofConstants.V_REFERENCE_NULL, "NULL");
		BUILD_IN_POF_TYPES.put(PofParser.T_PSEUDO_MAP_ENTRY, "ENTRY");
	}
	
	private static List<Rule> DEFAULT_RULES = new ArrayList<PofFinePrinter.Rule>();
	static {
		DEFAULT_RULES.add(new Rule(PofParser.T_PSEUDO_MAP_ENTRY, PofPath.root().a(0), "ENTRY", "key"));
		DEFAULT_RULES.add(new Rule(PofParser.T_PSEUDO_MAP_ENTRY, PofPath.root().a(1), "ENTRY", "value"));
	}
	
	
	private static String POF_PATH_REGEX = "(\\d+)[:](\\d+(.\\d+)*)";
	private static Pattern POF_PATH_PATTERN = Pattern.compile(POF_PATH_REGEX);
	
	private Map<Integer, String> typeNames = new HashMap<Integer, String>();
	private Map<Integer, Map<PofPath, Rule>> rules = new HashMap<Integer, Map<PofPath, Rule>>();
	
	public PofFinePrinter() {
		for(Rule r: DEFAULT_RULES) {
			addRule(r);
		}
		typeNames.putAll(BUILD_IN_POF_TYPES);
	}	

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
				String[] indexes = m.group(2).split("[.]");
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
	
	public String getClassName(int typeId) {
		return typeNames.get(typeId);
	}
	
	public Map<PofPath, String> findAliases(List<PofEntry> entries) {		
		Map<PofPath, String> result = new HashMap<PofPath, String>();
		result.put(PofPath.root(), "");
		
		TreeMap<PofPath, String> proposals = new TreeMap<PofPath, String>();
		
		for(PofEntry entry: entries) {

			String name = proposals.get(entry.getPath());
			if (name == null) {
				PofPath c = entry.getPath();
				while(true) {
					PofPath path = proposals.floorKey(c);
					if (path == null) {
						break;
					}
					PofPath epath = entry.getPath();
					if (epath.startsWith(path)) {
						name = proposals.get(path);
						name += epath.subpath(path.length());
						break;
					}
					else {
						c = c.subpath(0, c.length() - 1);
					}
				}
			}
			
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
		
		rules.clear();
		
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
