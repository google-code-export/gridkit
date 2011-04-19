/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.offheap.storage;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class __Linear {

    public static void main(String[] args) {
        System.out.println(splitHash(0, 5) + " " + splitHash(0, 6));
        System.out.println(splitHash(1, 5) + " " + splitHash(1, 6));
        System.out.println(splitHash(2, 5) + " " + splitHash(2, 6));
        System.out.println(splitHash(3, 5) + " " + splitHash(3, 6));
        System.out.println(splitHash(4, 5) + " " + splitHash(4, 6));
        System.out.println(splitHash(5, 5) + " " + splitHash(5, 6));
        System.out.println(splitHash(6, 5) + " " + splitHash(6, 6));
        System.out.println(splitHash(7, 5) + " " + splitHash(7, 6));
        System.out.println(splitHash(8, 5) + " " + splitHash(8, 6));
        System.out.println(splitHash(9, 5) + " " + splitHash(9, 6));
//        System.out.println(splitHash(1, 2, 4));
//        System.out.println(splitHash(2, 2, 4));
//        System.out.println(splitHash(3, 2, 4));
//        System.out.println(splitHash(4, 2, 4));
//        System.out.println(splitHash(5, 2, 4));
//        System.out.println(splitHash(6, 2, 4));
//        System.out.println(splitHash(7, 2, 4));
//        System.out.println(splitHash(8, 2, 4));
    }
    
    
    private static int splitHash(int hash, int capacity) {
        int round = Integer.highestOneBit(capacity);
        int split = capacity & ~round;

        int idx = hash % (round);
        
        if (idx >= split) {
            return idx;
        } else {
            return hash % (round << 1);
        }
    }

}
