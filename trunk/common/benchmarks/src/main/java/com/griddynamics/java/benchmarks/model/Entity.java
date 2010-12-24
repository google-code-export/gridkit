package com.griddynamics.java.benchmarks.model;

import java.util.Arrays;
import java.util.Random;

/**
 * represent abstract entity with vary size and lifetime
 * User: akondratyev
 * Date: Dec 16, 2010
 * Time: 7:01:35 PM
 */
public class Entity {
    private long[] weight;
    private static Random rnd = new Random();

    public Entity() {
    }

    public void setObjectSizeKb(int sizeKb) {
        weight = new long[(sizeKb * 128) + rnd.nextInt(20)];
    }

    public void setObjectSizeByte(int sizeBytes) {
        weight = new long[(sizeBytes >> 3) + rnd.nextInt(20)];
    }

    @Override
    public String toString() {
        return "Entity{" +
                "weight=" + (weight == null ? null : weight.length >> 7) +
                '}';
    }
}
