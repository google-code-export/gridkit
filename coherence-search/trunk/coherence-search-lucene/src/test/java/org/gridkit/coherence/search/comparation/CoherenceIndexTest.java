package org.gridkit.coherence.search.comparation;

import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;

import java.util.Set;

/**
 * @author Alexander Solovyov
 */

public class CoherenceIndexTest extends ComparationIndexTestBase {
    public ReflectionExtractor stringFieldExtractor;
    public ReflectionExtractor intFieldExtractor;

    @Override
    protected void setUp() {
        stringFieldExtractor = new ReflectionExtractor("getStringField");
        intFieldExtractor = new ReflectionExtractor("getIntField");

        cache.addIndex(stringFieldExtractor, false, null);
        cache.addIndex(intFieldExtractor, false, null);
    }

    protected Set entrySet() {
        return cache.entrySet(
                new AndFilter(
                        new EqualsFilter(stringFieldExtractor, "1"),
                        new EqualsFilter(intFieldExtractor, 1)));
    }

}
