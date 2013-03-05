package org.gridkit.coherence.chtest;

import org.junit.Rule;
import org.junit.rules.TestRule;

/**
 * Same as {@link CohCloud} but can be used as JUnit's {@link Rule}.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface CohCloudRule extends CohCloud, TestRule {

}
