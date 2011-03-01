package com.medx.framework.dictionary;

import com.medx.framework.dictionary.model.Dictionary;

public class DictionaryManager {
	private final Dictionary dictionary;

	public DictionaryManager(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	public Dictionary getDictionary() {
		return dictionary;
	}
}
