package org.gridkit.litter.fragmenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class GcLogParser {

	public static void main(String[] args) {
		for(String file: args) {
			File src = new File(file);
			File dest = new File(src.getParentFile(), src.getName() + ".extract");
			System.out.println("Refine " + file + "->" + dest.getPath());
			
			try {
				BufferedReader reader = new BufferedReader(new FileReader(src));
				Writer writer = new FileWriter(dest);
				StringBuffer buf = new StringBuffer();
				buf.append("1\t0\t");
				int num = 1;
				while(true) {
					String line = reader.readLine();
					if (line == null) {
						if (buf.length() > 0) {
							buf.append('\n');
							writer.append(buf);
						}
						break;
					}
					if (line.contains("ParNew")) {
						if (buf.length() > 0) {
							buf.append('\n');
							writer.append(buf);
							buf.setLength(0);
							
							buf.append(num++).append('\t');
							
							String timeStamp = line.substring(0,line.indexOf(':'));
							buf.append(timeStamp).append('\t');
						}
					}
					else if (line.startsWith("Total Free Space:") || line.startsWith("Max   Chunk Size:")) {
						line = line.substring(line.indexOf(':') + 1).trim();
						buf.append(line).append('\t');
					}
				}
				reader.close();
				writer.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
