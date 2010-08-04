package org.gridkit.coherence.search.comparation;

import com.tangosol.util.Filter;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;

import java.util.Set;

/**
 * @author Alexander Solovyov
 */

public class CoherenceIndexTest extends IndexComparisonTestBase {

    @Override
    protected void setUp() {
        for (int i = 0; i < N; i++) {
            cache.addIndex(stringFieldExtractors[i], false, null);
            cache.addIndex(intFieldExtractors[i], false, null);
        }
    }

    protected Set entrySet() {
        return cache.entrySet(
                new AndFilter(
                        new AllFilter(getStringFieldFilters()),
                        new AllFilter(getIntFieldFilters())));
    }

    private Filter[] getStringFieldFilters() {
        Filter[] filters = new Filter[N];

        for (int i = 0; i < N; i++) {
            filters[i] = new EqualsFilter(stringFieldExtractors[i], String.valueOf(i));
        }

        return filters;
    }

    private Filter[] getIntFieldFilters() {
        Filter[] filters = new Filter[N];

        for (int i = 0; i < N; i++) {
            filters[i] = new EqualsFilter(intFieldExtractors[i], i + N);
        }

        return filters;
    }
}
