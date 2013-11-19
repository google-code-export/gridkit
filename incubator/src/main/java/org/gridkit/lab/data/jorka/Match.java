package org.gridkit.lab.data.jorka;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.code.regexp.Matcher;

public class Match {
	
	Jorka 							jorka;
	Matcher 						match;
	int 							start;
	int 							end;	
	String							line;
	Garbage							garbage;
	
	private String 					text;
	private Map<String, Object> 	capture;
	
	/**
	 ** Contructor 
	 **/
	public Match(){
		text = "Nothing";
		jorka = null;
		match = null;
		capture = new TreeMap<String, Object>();
		garbage = new Garbage();
		start = 0;
		end = 0;
	}
	
	
	/**
	 * 
	 * @param line to analyze / save
	 * @return
	 */
	public void setText( String text ) {
		if( text == null || text.isEmpty()) {
			throw new IllegalArgumentException("subject should not be empty");
		}
		this.text = text;
	}
	
	/**
	 * Getter
	 * @return the subject
	 */
	public String getText(){
		return text;
	}
	
	/**
	 * Match to the <tt>subject</tt> the <tt>regex</tt> and save the matched element into a map
	 * 
	 * @see getSubject
	 * @see toJson
	 */
	public void parse(){
		if( this.match == null ) {
			throw new IllegalStateException("Not matched yet");
		}
		
		Map<String, String> mappedw = this.match.namedGroups();
		Iterator<Entry<String, String>> it = mappedw.entrySet().iterator();
	    while (it.hasNext()) {
	       
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	        String key = null;
	        Object value = null;
	        if ( !this.jorka.getCaptured().get(pairs.getKey().toString()).isEmpty() ) {
	        	key = this.jorka.getCaptured().get(pairs.getKey().toString());
	        }
	        if( pairs.getValue() != null ){
	        	value = pairs.getValue().toString();
	        	if( this.isInteger( value.toString() ) )
	        		value = Integer.parseInt( value.toString() );
	        	else
	        		value = cleanString(pairs.getValue().toString());
	        }
	        capture.put( key  , (Object)value);
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	}
	
	
	/**
	 * remove from the string the quote and dquote
	 * 
	 * @param string to pure: "my/text"
	 * @return unquoted string: my/text
	 */
	private String cleanString( String value ){
		if( value == null || value.isEmpty() )
			return value;
		char[] tmp = value.toCharArray();
    	if( (tmp[0] == '"' && tmp[value.length()-1] == '"')
    		|| (tmp[0] == '\'' && tmp[value.length()-1] == '\''))
    		value = value.substring(1, value.length()-1);
    	return value;
	}
		
	/**
	 * @return java map object from the matched element in the text
	 */
	public Map<String, Object> toMap(){
		this.cleanMap();
		return capture;
	}

	/**
	 * @return java map object from the matched element in the text
	 */
	public String toJSON(){
		StringWriter writer = new StringWriter();
		try {
			new JsonWriter(writer).writeMap(capture);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return writer.toString();
	}
	
	/**
	 * remove and/or rename items 
	 */
	private void cleanMap(){
		garbage.rename(capture);
		garbage.remove(capture);
	}
	
	/**
	 */
	public Boolean isNull(){
		if( this.match == null )
			return true;
		return false;
	}
	
	private boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
}
