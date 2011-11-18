package org.gridkit.coherence.search.comparation;

import java.util.Random;

import org.junit.Ignore;

@Ignore
public class PostGenerator {

	private int totalAuthors;
	private int categories;

	public PostGenerator() {		
	}
	
	public void init() {
		
	}
	
	public SyntheticPostDocument generate(int seqNo) {
		SyntheticPostDocument doc = new SyntheticPostDocument();
		doc.setId(seqNo);
		doc.setCategory(randomCategory(seqNo));
		doc.setAuthor(randomAuthor(seqNo));
		
		return doc;
	}

	private String randomAuthor(int seqNo) {
		Random rnd = new Random(seqNo + 1);
		int uid = rnd.nextInt(totalAuthors);
		return "user" + uid;
		
	}

	private int randomCategory(int seqNo) {
		Random rnd = new Random(seqNo + 2);
		return rnd.nextInt(categories);
	}
	
}
