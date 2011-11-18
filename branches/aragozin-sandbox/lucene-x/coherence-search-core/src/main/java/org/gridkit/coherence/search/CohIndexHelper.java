package org.gridkit.coherence.search;

import java.util.Map;

import com.tangosol.util.MapTrigger;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.AbstractExtractor;

public class CohIndexHelper {

	@SuppressWarnings("rawtypes")
	public static Object extractFromEntryOrValue(Map.Entry entry, ValueExtractor extractor) {
		if (extractor instanceof AbstractExtractor) {
			return ((AbstractExtractor)extractor).extractFromEntry(entry);
		}
		else {
			return extractor.extract(entry.getValue());
		}
	}

	public static Object extractFromOriginalValue(MapTrigger.Entry entry, ValueExtractor extractor) {
		if (extractor instanceof AbstractExtractor) {
			return ((AbstractExtractor)extractor).extractOriginalFromEntry(entry);
		}
		else {
			return extractor.extract(entry.getOriginalValue());
		}		
	}
}
