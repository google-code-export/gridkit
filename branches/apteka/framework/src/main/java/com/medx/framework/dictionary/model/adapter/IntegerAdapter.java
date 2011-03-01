package com.medx.framework.dictionary.model.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class IntegerAdapter extends XmlAdapter<String, Integer> {
    public Integer unmarshal(String str) {
        return Integer.parseInt(str);
    }

    public String marshal(Integer number) {
        if (number == null)
        	throw new IllegalArgumentException("number == null");

        return number.toString();
    }
}
