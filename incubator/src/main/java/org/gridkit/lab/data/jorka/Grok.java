package org.gridkit.lab.data.jorka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.TreeMap;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;

@SuppressWarnings("unused")
public class Grok extends Object {
	
	// manage string like %{Foo} => Foo
	private static java.util.regex.Pattern	PATTERN = java.util.regex.Pattern.compile("%\\{(.*?)\\}");
	private static Pattern 						
			PATTERN_RE = Pattern.compile("%\\{" +
											"(?<name>"+
												"(?<pattern>[A-z0-9]+)"+
													"(?::(?<subname>[A-z0-9_:]+))?"+
											")"+
											"(?:=(?<definition>"+
													"(?:"+
														"(?:[^{}]+|\\.+)+"+
													")+" +
												 ")" +
											")?"+
											"\\}");

	//Public
	public Map< String,String > 			patterns;
	public String 							saved_pattern = null;
	
	//Private
	private Map< String,String > 			captured_map;
	
	private String 							expanded_pattern;
	private String 							pattern_origin;
	private Pattern 						regexp;
	private Discovery 						disco;
	
	/**
	 ** Constructor.
	 **/
	public		Grok(){	
		
		pattern_origin = null;
		disco = null;
		expanded_pattern = null;
		regexp = null;
		patterns = new TreeMap<String, String>();
		captured_map = new TreeMap<String, String>();
	}
	
	/**
	 * Add a new pattern
	 * 
	 *  @param name Name of the pattern
	 *  @param pattern regex string
	 **/
	public int addPattern( String name, String pattern){
		if( name.isEmpty() || pattern.isEmpty() )
			return GrokError.GROK_ERROR_UNINITIALIZED;
		patterns.put(name, pattern);
		return GrokError.GROK_OK;
	}
	
	/**
	 * Copy the map patterns into the grok pattern
	 * 
	 * @param Map of the pattern to copy
	 **/
	public int copyPatterns( Map<String, String> cpy ){
		if( cpy.isEmpty() || cpy == null)
			return GrokError.GROK_ERROR_UNINITIALIZED;
		for (Map.Entry<String, String> entry : cpy.entrySet())
	        patterns.put(entry.getKey().toString(), entry.getValue().toString());
		return GrokError.GROK_OK;
	}
	
	/**
	 * @return the current map grok patterns
	 */
	public Map< String,String > getPatterns(){
		return this.patterns;
	}
	
	/**
	 * @return the compiled regex of <tt>expanded_pattern</tt>
	 * @see compile
	 */
	public Pattern getRegEx(){
		return regexp;
	}
	
	/**
	 * 
	 * @return the string pattern
	 * @see compile
	 */
	public String getExpandedPattern(){
		return expanded_pattern;
	}
	
	/**
	 * Add patterns to grok from a file
	 * 
	 * @param file that contains the grok patterns
	 * @throws Throwable
	 */
	public int addPatternFromFile( String file) throws Throwable{
		
		File f = new File(file);
		if(!f.exists())
			return GrokError.GROK_ERROR_FILE_NOT_ACCESSIBLE;
		if( !f.canRead() )
			return GrokError.GROK_ERROR_FILE_NOT_ACCESSIBLE;
		
		return addPatternFromReader(new FileReader(f));
	}
	
	/**
	 * Add patterns to grok from a reader
	 * 
	 * @param reader that contains the grok patterns
	 * @throws Throwable
	 */
	public int addPatternFromReader(Reader r) throws Throwable{
		BufferedReader br = new BufferedReader(r);
        String line;
        //We dont want \n and commented line
        Pattern MY_PATTERN = Pattern.compile("^([A-z0-9_]+)\\s+(.*)$");
        while((line = br.readLine()) != null) {
        	Matcher m = MY_PATTERN.matcher(line);
        	if( m.matches() )
        		this.addPattern( m.group(1), m.group(2) );
        }
        br.close();
        return GrokError.GROK_OK;
	}
	
	/**
	 * Match the <tt>text</tt> with the pattern
	 * 
	 * @param text to match
	 * @return Grok Match
	 * @see Match
	 */
	public Match match( String text ){
		
		if( regexp == null)
			return null;

		Matcher m = regexp.matcher(text);
		Match match = new Match();
		//System.out.println(expanded_pattern);
		if( m.find() )
		{		
			//System.out.println("LLL"+m.group() +" " + m.start(0) +" "+ m.end(0));
			match.setSubject(text);
			match.grok = this;
			match.match = m;
			match.start = m.start(0);
			match.end = m.end(0);
			match.line = text;
			return match;
		}
		return match;
	}
	
	/**
	 * Transform grok regex into a compiled regex
	 * 
	 * @param Grok pattern regex
	 */
	public int compile( String pattern ){
		expanded_pattern = new String( pattern );
		pattern_origin = new String( pattern );
		int index = 0;
		Boolean Continue = true;
		
		//Replace %{foo} with the regex (mostly groupname regex) 
		//and then compile the regex
		while (Continue){
			Continue=false;
			
			Matcher m = PATTERN_RE.matcher(expanded_pattern);
			// Match %{Foo:bar} -> pattern name and subname
			// Match %{Foo=regex} -> add new regex definition
			if (m.find() ){
				Continue = true;
				Map<String, String> group = m.namedGroups();

				if(group.get("definition") != null){
					addPattern(group.get("pattern"), group.get("definition"));
					group.put("name", group.get("name") +"="+ group.get("definition") );
					//System.out.println("%{"+group.get("name")+"} =>" + this.patterns.get(group.get("pattern")));
				}
				captured_map.put( "name"+index, (group.get("subname") != null ? group.get("subname"):group.get("name")));
				expanded_pattern = expanded_pattern.replace((CharSequence)"%{"+group.get("name")+"}", "(?<name"+index+">" + this.patterns.get(group.get("pattern"))+")");
				//System.out.println(_expanded_pattern);
				index++;
			}			
		}
		//System.out.println(_captured_map);
		//Compile the regex
		if(!expanded_pattern.isEmpty()){
			regexp = Pattern.compile(expanded_pattern);
			return GrokError.GROK_OK;
		}
		return GrokError.GROK_ERROR_PATTERN_NOT_FOUND;
	}
	
	/**
	 * Grok can find the pattern
	 * 
	 * @param input the file to analyze
	 * @return the grok pattern
	 */
	public String discover( String input ){
		
		if (disco == null )
			disco = new Discovery( this );
		return disco.discover(input);
	}
	
	/**
	 * 
	 * @param Key
	 * @return the value
	 */
	public String capture_name( String id ){
		return captured_map.get(id);
	}

	/**
	 * 
	 * @return getter
	 */
	public Map<String, String> getCaptured(){
		return captured_map;
	}
	
	/**
	 ** Checkers 
	 **/
	public int isPattern(){
		if( patterns == null )
			return 0;
		if(patterns.isEmpty())
			return 0;
		return 1;
	}
}
