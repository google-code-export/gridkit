package org.gridkit.lab.orchestration;

import java.io.Serializable;

public interface Printer extends Generic<Printer> {
    void err(Object str);
    
    void out(Object str);
    
    String i(String str);
    
    void sleep(long millis);
    
    Runnable function();
    
    void exception();
    
    @SuppressWarnings("serial")
    public static class Impl implements Printer, Serializable {
        @Override
        public void err(Object str) {
            System.err.println(str);
        }

        @Override
        public void out(Object str) {
            System.out.println(str);
        }
        
        @Override
        public String toString() {
            return "Printer$Impl";
        }

        @Override
        public String i(String str) {
            System.out.println("i(" + str + ")");
            return str;
        }

        @Override
        public Runnable function() {
            return new Runnable() {
                @Override
                public void run() {
                    System.err.println("non serializable function");
                }
            };
        }
        
        @Override
        public void exception() {
            throw new IllegalStateException();
        }

        @Override
        public Printer getGeneric() {
            return this;
        }

        @Override
        public void sleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
