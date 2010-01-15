/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package com.griddynamics.gridkit.coherence.patterns.command.internal;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public interface IdGenerator<E> {    
    public E nextId();
}
