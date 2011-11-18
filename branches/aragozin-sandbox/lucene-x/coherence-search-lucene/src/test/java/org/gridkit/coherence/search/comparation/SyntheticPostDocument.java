package org.gridkit.coherence.search.comparation;

import java.io.Serializable;

public class SyntheticPostDocument implements Serializable {

	private static final long serialVersionUID = 20101112L;
	
	private int id;
	private String title;
	private String author;
	private long posted;
	private int category;
	private int[] replayTo;
	private String[] tags;
	
	public SyntheticPostDocument() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public long getPosted() {
		return posted;
	}

	public void setPosted(long posted) {
		this.posted = posted;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int[] getReplayTo() {
		return replayTo;
	}

	public void setReplayTo(int[] replayTo) {
		this.replayTo = replayTo;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}
}
