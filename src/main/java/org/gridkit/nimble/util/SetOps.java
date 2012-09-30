package org.gridkit.nimble.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SetOps {
    public static Set<String> intersection(Collection<String> c1, Collection<String> c2) {
        Set<String> result = new HashSet<String>(c1);
        result.retainAll(c2);
        return result;
    }
}
