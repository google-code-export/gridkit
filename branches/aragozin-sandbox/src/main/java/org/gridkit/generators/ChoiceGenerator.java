package org.gridkit.generators;

import java.util.Random;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ChoiceGenerator<V> implements DeterministicObjectGenerator<V> {

	private V[] choices;
	private Random rnd = new Random();
	
	public ChoiceGenerator(V... choices) {
		this.choices = choices;
	}

	@Override
	public V object(long id) {
		return choices[rnd.nextInt(choices.length)];
	}
	
	public ChoiceGenerator<V> clone() {
		return new ChoiceGenerator<V>(choices);
	}	
}
