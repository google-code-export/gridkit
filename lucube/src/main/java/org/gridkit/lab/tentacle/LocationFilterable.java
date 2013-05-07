package org.gridkit.lab.tentacle;


/**
 * <p>
 * Special interface to declare filter option.
 * </p>
 * <p> 
 * In source code filter methods are applied to {@link Source} objects, 
 * but in runtime they will be invoked on {@link Locator} objects.
 * </p>
 * <p>
 * This interface used to share declaration and ensure some
 * level of compile time safety.
 * </p>
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 * @param <X> eigther {@link Source} or {@link Locator}
 */
public interface LocationFilterable<X> {

}
