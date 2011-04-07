package sample;

import java.util.Map;

public interface ObjectGenerator<A, B> {
    
    public Map<A, B>  generate(long from, long to);

}
