package org.gridkit.generators;

import java.util.Random;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class StringGenerator implements DeterministicObjectGenerator<String> {

	private static char[] CHARACTERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	
	private int lengthAvg;
	private int lengthStdDev;
	private char[] alfabet;
	private Random rnd = new Random();
	private char[] charBuf;
	
	public StringGenerator(int lengthAvg, int lengthStdDev) {
		this(lengthAvg, lengthStdDev, CHARACTERS);
	}

	public StringGenerator(int lengthAvg, int lengthStdDev, char[] alfabet) {
		this.lengthAvg = lengthAvg;
		this.lengthStdDev = lengthStdDev;
		this.alfabet = alfabet;
		this.charBuf = new char[lengthAvg + 2 * lengthStdDev];
	}

	@Override
	public String object(long id) {
		rnd.setSeed(id);
		int len = (int) (lengthAvg + rnd.nextGaussian() * lengthStdDev);
		if (len < 0) {
			len = 0;
		}
		char[] chars = len > charBuf.length ? new char[len] : charBuf;
		for(int i = 0; i != len; ++i) {
			chars[i] = alfabet[rnd.nextInt(alfabet.length)];
		}
		return new String(chars, 0, len);
	}

	@Override
	public StringGenerator clone() {
		return new StringGenerator(lengthAvg, lengthStdDev, alfabet);
	}
}
