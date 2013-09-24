package org.gridkit.lab.data.jorka;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Pile {

	//Private
	private List<Grok>					_groks;
	private Map<String,String> 			_patterns;
	private List<String>				_pattern_files;
	
	static final String 				defaultPatternDirectory = "patterns/";
	
	/**
	 ** Constructor
	 **/
	public Pile() {
		_patterns = new TreeMap<String, String>();
		_groks = new ArrayList<Grok>();
		_pattern_files = new ArrayList<String>();
	}
	
	/**
	 * 
	 * @param name of the pattern
	 * @param file path
	 * @return
	 */
	public int	addPattern(String name, String file){
		if( name.isEmpty() || file.isEmpty() )
			return GrokError.GROK_ERROR_UNINITIALIZED;
		_patterns.put(name, file);
		return GrokError.GROK_OK;
	}
	
	/**
	 * Load patterns file from a directory
	 * 
	 * @param directory
	 */
	public int	addFromDirectory(String directory){
		
		if(directory == null || directory.isEmpty() )
			directory = defaultPatternDirectory;
		
		File dir = new File(directory.toString());
		File lst[] = dir.listFiles();
		
		for( int i = 0; i < lst.length; i++ )
			if( lst[i].isFile() )
		addPatternFromFile(lst[i].getAbsolutePath());
		
		return GrokError.GROK_OK;
	}
	
	
	/**
	 * Add pattern to grok from a file
	 * 
	 * @param patch file
	 * @return
	 */
	public int	addPatternFromFile(String file){
		
		File f = new File(file);
		if(!f.exists())
			return GrokError.GROK_ERROR_FILE_NOT_ACCESSIBLE;
		_pattern_files.add(file);
		return GrokError.GROK_OK;
	}
	
	/**
	 * Compile the pattern with a corresponding grok
	 * 
	 * @param pattern
	 * @throws Throwable
	 */
	public void	compile( String pattern ) throws Throwable{
		
		Grok grok = new Grok();
		
		Map<String, String> map = new TreeMap<String, String>();
		
		for(Map.Entry<String, String> entry : _patterns.entrySet())
	        if (!map.containsValue((entry.getValue())))
	        	grok.addPattern(entry.getKey().toString(), entry.getValue().toString());
		
		for (String file : _pattern_files)
			grok.addPatternFromFile(file);
		
		grok.compile(pattern);
		_groks.add(grok);
	}
	
	/**
	 * @param line to match
	 * @return Grok Match
	 */
	public Match	match( String line ){
		for (Grok grok : _groks){
			Match gm = grok.match(line);
			if( gm != null )
				return gm;
		}
		
		return null;
	}
	
}
