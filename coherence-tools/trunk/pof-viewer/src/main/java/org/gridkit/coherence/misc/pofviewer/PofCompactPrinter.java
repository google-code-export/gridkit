package org.gridkit.coherence.misc.pofviewer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.tangosol.io.pof.PofConstants;

public class PofCompactPrinter {

	private PofFinePrinter finePrinter;
	
	public PofCompactPrinter(PofFinePrinter finePrinter) {
		this.finePrinter = finePrinter;
	}
	
	public String format(List<PofEntry> entries) {
		Map<PofPath, String> aliases;
		if (finePrinter != null) {
			aliases = finePrinter.findAliases(entries);
		}
		else {
			aliases = Collections.emptyMap();
		}
		StringBuilder sb = new StringBuilder();
		format(sb, new LinkedList<PofEntry>(entries), aliases);
		
		return sb.toString();
	}

	private void format(StringBuilder sb, List<PofEntry> entries, Map<PofPath, String> aliases) {
		PofEntry root = entries.remove(0);
		if (root.getTypeId() > 0) {
			String type = finePrinter == null ? finePrinter.getClassName(root.getTypeId()) : null;
			if (type == null) {
				type = "(" + root.getTypeId() + ")";
			}
			sb.append(type);
		}
		if (root.getTypeId() == PofConstants.V_REFERENCE_NULL) {
			sb.append("null");
		}
		else if (root.getTypeId() == PofConstants.V_COLLECTION_EMPTY) {
			sb.append("{}");
		}
		if (root.getValue() != null) {
			sb.append(String.valueOf(root.getValue()));
		}
		boolean brace = false;
		while(!entries.isEmpty()) {
			if (entries.get(0).getPath().startsWith(root.getPath())) {
				if (!brace) {
					sb.append("{");
					brace = true;
				}
				else {
					sb.append(", ");
				}
				PofEntry e = entries.get(0);
				String field = aliases.get(e.getPath());
				if (field == null) {
					field = String.valueOf(e.getPath().lastIndex()); 
				}
				sb.append(field).append(":");
				format(sb, entries, aliases);
			}
			else {
				break;
			}
		}
		if (brace) {
			sb.append("}");
		}
	}
	
}
