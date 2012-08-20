package org.gridkit.lab.avalanche.iris.facts;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.deri.iris.api.terms.ITerm;

public class Bean implements IBean {

	
	private Map<String, ITerm> attributes = new LinkedHashMap<String, ITerm>();
	
	@Override
	public int compareTo(ITerm o) {
		return 0;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ITerm) {
			return compareTo(ITerm o)
		}
		return super.equals(obj);
	}

	@Override
	public URI getDatatypeIRI() {
		return null;
	}

	@Override
	public String toCanonicalString() {
		return toString();
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public Object getValue() {
		return ;
	}
}
