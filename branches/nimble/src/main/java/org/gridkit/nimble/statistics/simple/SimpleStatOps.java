package org.gridkit.nimble.statistics.simple;

import org.gridkit.nimble.util.Pair;

public class SimpleStatOps {
    public static final String MARK_SEP = "^"; // sampler^value^aggregate
    
    public static boolean isMarked(String str) {
        int firstIndex = str.indexOf(MARK_SEP);
        
        if (firstIndex == -1 || firstIndex == 0 || isLast(firstIndex, str)) {
            return false;
        }
        
        int secondIndex = str.indexOf(MARK_SEP, firstIndex + 1);
        
        if (secondIndex == -1 || isLast(secondIndex, str)) {
            return false;
        }
        
        return true;
    }
    
    public static String mark(String sampler, String value, String aggregate) {
        return sampler + MARK_SEP + value + MARK_SEP + aggregate;
    }
    
    private static boolean isLast(int index, String str) {
        return index == str.length() - 1;
    }

    public static String getSampler(String markedStr) {
        Pair<Integer, Integer> indexes = getMarkIndexes(markedStr);
        
        return markedStr.substring(0, indexes.getA());
    }
    
    public static String getValue(String markedStr) {
        Pair<Integer, Integer> indexes = getMarkIndexes(markedStr);
        
        return markedStr.substring(indexes.getA() + 1, indexes.getB());
    }
    
    public static String getAggregate(String markedStr) {
        Pair<Integer, Integer> indexes = getMarkIndexes(markedStr);
        
        return markedStr.substring(indexes.getB() + 1);
    }
    
    private static Pair<Integer, Integer> getMarkIndexes(String markedStr) {
        int firstIndex = markedStr.indexOf(MARK_SEP);
        int secondIndex = markedStr.indexOf(MARK_SEP, firstIndex + 1);
        
        return Pair.newPair(firstIndex, secondIndex);
    }
}
