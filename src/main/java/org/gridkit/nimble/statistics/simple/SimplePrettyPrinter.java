package org.gridkit.nimble.statistics.simple;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimplePrettyPrinter extends SimplePrinter {
    public SimplePrettyPrinter() {
        this(Collections.<String>emptyList(), Collections.<String>emptyList());
    }
    
    public SimplePrettyPrinter(List<String> leftHeader, List<String> rightHeader) {
        super(leftHeader, rightHeader);
    }

    @Override
    protected void printTable(PrintStream stream, List<List<String>> rows) {
        List<Integer> lens = columnLens(rows);
        
        for (List<String> row : rows) {
            stream.print("| ");
            
            for (int c = 0; c < lens.size(); ++c) {
                String cell = c < row.size() ? row.get(c) : "";
                stream.printf("%" + lens.get(c) + "s | ", cell);
            }
            
            stream.println();
        }
    }
    
    private static List<Integer> columnLens(List<List<String>> rows) {
        int maxSize = maxSize(rows);
        
        List<Integer> lens = new ArrayList<Integer>(maxSize);
        
        for (int c = 0; c < maxSize; ++c) {
            int len = Integer.MIN_VALUE;
            
            for (int r = 0; r < rows.size(); ++r) {
                List<String> row = rows.get(r);
                
                if (c < row.size()) {
                    len = Math.max(len, row.get(c).length());
                }
            }
            
            lens.add(len);
        }
        
        return lens;
    }
    
    private static int maxSize(List<List<String>> rows) {
        int max = Integer.MIN_VALUE;
        
        for (List<String> list : rows) {
            max = Math.max(max, list.size());
        }
        
        return max;
    }
}
