package azul.test.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Record implements Serializable {
	private static final long serialVersionUID = -6914575930058266644L;

	protected Long id;
	protected String first;
	protected String second;
	protected byte[] bytes;
	protected List<Integer> ints;

	public Record() {

	}

	public Record(int size, int dispersion) {
		int[] sizes = new int[4];
		
		Random rand = new Random();
		
		if (rand.nextBoolean())
			size += rand.nextInt(dispersion);
		else
			size -= rand.nextInt(dispersion);
		
		size -= 4;
		
		id = rand.nextLong();
		int remainder = size % 4;
		
		size /= 4;
		for (int i = 0, j = sizes.length - 2; i < sizes.length - 1; i++, j--) {
			sizes[i] = rand.nextInt(size - j);
			size -= sizes[i];
		}
		
		sizes[sizes.length - 1] = size;
		char[] chars = new char[sizes[0] * 2];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) ('A' + rand.nextInt(26));
		}
		
		first = new String(chars);
		chars = new char[sizes[1] * 2];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) ('A' + rand.nextInt(26));
		}
		
		second = new String(chars);
		bytes = new byte[sizes[2] * 4 + remainder];
		rand.nextBytes(bytes);
		
		ints = new LinkedList<Integer>();
		for (int i = 0; i < sizes[3]; i++) {
			ints.add(rand.nextInt());
		}
	}

	public Long getId() {
		return id;
	}

	public String getFirst() {
		return first;
	}

	public String getSecond() {
		return second;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public List<Integer> getInts() {
		return ints;
	}
}
