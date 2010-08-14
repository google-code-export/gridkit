package org.gridkit.coherence.search.comparation;

import com.tangosol.util.Filter;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;

import java.util.Set;

/**
 * @author Alexander Solovyov
 */

public class NoIndexTest extends IndexComparisonTestBase {

    public static void main(String[] args) {
		configure();

		NoIndexTest test = new NoIndexTest();
		test.init();
		System.out.println("No index");
		System.out.println("N = " + N);
		System.out.println("RECORD_NUMBER = " + RECORD_NUMBER);
		test.test();
	}
	
    @Override
    protected void setUp() {
//		System.out.println("Creating Coherence indexes");
//        for (int i = 0; i < N; i++) {
//            cache.addIndex(stringFieldExtractors[i], false, null);
//            cache.addIndex(intFieldExtractors[i], false, null);
//        }
    }

    @SuppressWarnings("unchecked")
	protected Set entrySet() {
        return cache.entrySet(
                new AndFilter(
                        new AllFilter(getStringFieldFilters()),
                        new AllFilter(getIntFieldFilters())));
    }

    private Filter[] getStringFieldFilters() {
        Filter[] filters = new Filter[N];

        for (int i = 0; i < N; i++) {
            filters[i] = new EqualsFilter(stringFieldExtractors[i], "A");
        }

        return filters;
    }

    private Filter[] getIntFieldFilters() {
        Filter[] filters = new Filter[N];

        for (int i = 0; i < N; i++) {
            filters[i] = new EqualsFilter(intFieldExtractors[i], 0);
        }

        return filters;
    }
}
