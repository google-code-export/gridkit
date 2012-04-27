package org.gridkit.coherence.misc.bulletproof;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.tangosol.io.pof.PofConstants;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.Filter;
import com.tangosol.util.filter.IndexAwareFilter;

/**
 * @author Alexey Ragozin
 */
class IncludeCanaryFilter implements IndexAwareFilter {

	private Filter nestedFilter;
	
	private boolean pofMode;
	private int typeId;
	
	IncludeCanaryFilter(Filter nestedFilter, int typeId) {
		this.nestedFilter = nestedFilter;
		this.typeId = typeId;
		this.pofMode = typeId != PofConstants.T_UNKNOWN;
	}
	
	@Override
	public boolean evaluate(Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean evaluateEntry(Entry entry) {
		return !(entry.getKey() instanceof CanaryKey);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Filter applyIndex(Map indexes, Set candidateSet) {
		if (nestedFilter != null) {
			if (nestedFilter instanceof IndexAwareFilter) {
				((IndexAwareFilter)nestedFilter).applyIndex(indexes, candidateSet);
			}
		}
		if (pofMode) {
			filterPofKeys(candidateSet);
		}
		else {
			filterObjectKeys(candidateSet);
		}
		return nestedFilter;
	}

	@SuppressWarnings("rawtypes")
	private void filterObjectKeys(Set candidateSet) {
		Iterator it = candidateSet.iterator();
		while(it.hasNext()) {
			Object k = it.next();
			if (k instanceof CanaryKey) {
				it.remove();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void filterPofKeys(Set candidateSet) {
		Iterator it = candidateSet.iterator();
		while(it.hasNext()) {
			Binary bk = (Binary) it.next();
			bk = ExternalizableHelper.getDecoration(bk, 0); // undecorate
			try {
				int typeId = bk.getBufferInput().readPackedInt();
				if (typeId == this.typeId) {
					it.remove();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public int calculateEffectiveness(Map indexes, Set candidateSet) {
		throw new UnsupportedOperationException();
	}
}
