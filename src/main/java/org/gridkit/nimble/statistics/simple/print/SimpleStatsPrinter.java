package org.gridkit.nimble.statistics.simple.print;

public interface SimpleStatsPrinter {
    void print(Contetx context);
    
    public interface Contetx {
        void newline();
        
        void cell(String name, Object object);
    }
}
