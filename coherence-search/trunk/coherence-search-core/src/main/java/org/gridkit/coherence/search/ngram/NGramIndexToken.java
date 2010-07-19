package org.gridkit.coherence.search.ngram;

public class NGramIndexToken {

    public static final NGramIndexToken INSTANCE = new NGramIndexToken();
    
    public NGramIndexToken() {
        // for remotting should be public
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }
    
    
}
