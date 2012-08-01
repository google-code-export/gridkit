package org.gridkit.nimble.statistics.simple;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SimpleCsvPrinter extends SimplePrinter {
    public static final char DEFAULT_SEPARATOR = ';';
    
    private char separator;

    public SimpleCsvPrinter() {
        this(Collections.<String>emptyList(), Collections.<String>emptyList());
    }
    
    public SimpleCsvPrinter(List<String> leftHeader, List<String> rightHeader) {
        this(DEFAULT_SEPARATOR, leftHeader, rightHeader);
    }
    
    public SimpleCsvPrinter(char separator, List<String> leftHeader, List<String> rightHeader) {
        super(leftHeader, rightHeader);
        this.separator = separator;
    }

    @Override
    protected void printTable(PrintStream stream, List<List<String>> table) {
        for (List<String> row : table) {
            Iterator<String> iter = row.iterator();
            
            while (iter.hasNext()) {
                stream.print(iter.next());
                
                if (iter.hasNext()) {
                    stream.print(separator);
                }
            }
            
            stream.println();
        }
    }
}
