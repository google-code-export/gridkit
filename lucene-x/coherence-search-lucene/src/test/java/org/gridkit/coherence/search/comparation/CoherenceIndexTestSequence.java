package org.gridkit.coherence.search.comparation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.BetweenFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.InFilter;

public class CoherenceIndexTestSequence extends BaseTestSequence {
	
	
	public void createIndexes(NamedCache cache, List<String> fields) {
		for (String attrib: fields) {
			ValueExtractor e = new ReflectionExtractor("get", new Object[]{attrib});
			if (attrib.endsWith("s")) {
				cache.addIndex(e, true, null);
			}
			else {
				cache.addIndex(e, false, null);
			}
		}
	}
	
	public Filter createFilter(QueryCondition[] conditions) {
		if (conditions.length == 1) {
			return createFilter(conditions[0]);
		}
		else {
			Filter[] filters = new Filter[conditions.length];
			for(int i = 0; i != conditions.length; ++i) {
				filters[i] = createFilter(conditions[i]);
			}
			return new AllFilter(filters);
		}
	}

	private Filter createFilter(QueryCondition qc) {
		ValueExtractor e = new ReflectionExtractor("get", new Object[]{qc.field});
		if (qc.rangeQuery) {
			if (System.getProperty("use-BetweenFilter") != null) {
				return new BetweenFilter(e, qc.terms[0], qc.terms[1]);
			}
			else {
				return new RangeFilter(e, qc.terms[0], qc.terms[1], true, false, null);
			}
		}
		else if (qc.terms.length == 1) {
			return new EqualsFilter(e, qc.terms[0]);
		}
		else {
			Set<String> set = new HashSet<String>(Arrays.asList(qc.terms));
			return new InFilter(e, set);
		}
	}

	public static void main(String[] args) {
		new CoherenceIndexTestSequence().start();
	}
	
}
