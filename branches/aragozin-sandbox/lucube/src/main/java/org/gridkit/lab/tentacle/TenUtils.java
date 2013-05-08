package org.gridkit.lab.tentacle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gridkit.lab.tentacle.MonitoringSchema.MonitoringConfig;

class TenUtils {

	public static String toSafeString(Object ss) {
		if (ss == null) {
			return "null";
		}
		String name = null;
		try {
			name = ss.toString();
		}
		catch(ThreadDeath e) {
			throw e;
		}
		catch(OutOfMemoryError e) {
			throw e;
		}
		catch(Throwable e) {
			// ignore;
		}
		if (name == null) {
			name = ss.getClass().getName() + "@" + System.identityHashCode(ss);
		}
		return name;
	}
	
	
	public static String generateId(int n) {
		if (n < 0) {
			throw new IllegalArgumentException(n + " is negative");
		}
		if (n <= 99) {
			return String.format("%02d", n);
		}
		else if (n <= 999) {
			return String.format("A%03d", n);
		}
		else if (n <= 9999) {
			return String.format("B%04d", n);
		}
		else if (n <= 99999) {
			return String.format("C%05d", n);
		}
		else if (n <= 999999) {
			return String.format("D%06d", n);
		}
		else if (n <= 9999999) {
			return String.format("D%07d", n);
		}
		else {
			throw new IllegalArgumentException(n + " is too large");
		}
	}
	
	public static String indent(String string, int indent) {
		StringBuilder sb = new StringBuilder();
		
		Pattern regex = Pattern.compile("(\n)|(<[^/>]+>)|(<[/][^/>]+>)|(<[^/>]+[/]>)");
		Matcher m = regex.matcher(string);
		int ci = 0;
		int p = 0;
		while(true) {
			if (m.find(p)) {
				if (m.start() > p) {
					sb.append(string.substring(p, m.start()));
				}				
				if ("\n".equals(m.group())) {
					sb.append("\n");
					for(int i = 0; i < ci * indent; ++i) {
						sb.append(' ');
					}
				}
				else if (m.group().endsWith("/>")) {
					sb.append(m.group());
				}
				else if (m.group().startsWith("</")) {
					--ci;
					// undo indentation before closing tag
					for(int i = 0; i < indent; ++i) {
						if (sb.charAt(sb.length() - 1) == ' ') {
							sb.setLength(sb.length() - 1);
						}
					}
					sb.append(m.group());
				}
				else {
					++ci;
					sb.append(m.group());
				}
				p = m.end();
			}
			else {
				sb.append(string.substring(p, string.length()));
				break;
			}
		}
		
		return sb.toString();
	}	
}
