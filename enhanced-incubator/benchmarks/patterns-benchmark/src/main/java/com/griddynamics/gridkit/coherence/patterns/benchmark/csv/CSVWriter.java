/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.griddynamics.gridkit.coherence.patterns.benchmark.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Locale;

public final class CSVWriter
{
	private static final String  frenchSpace = Character.toString((char)160);
	private static final NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
	
	public static String formatDoubleToCSV(double d)
	{
		return format.format(d).replaceAll(frenchSpace, "");
	}
	
	private final BufferedWriter out;
	
	public CSVWriter(String fileName)
	{
		File file = new File(fileName);
        
		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(file);
		}
		catch(FileNotFoundException e)
		{
			throw new RuntimeException("CSVWriter.FileNotFoundException (" + fileName + ")", e);
		}
		
		try
		{
			out = new BufferedWriter(new OutputStreamWriter(fos,"ansi-1251"));
		}
		catch (UnsupportedEncodingException e)
		{
			try { fos.close(); } catch (IOException ignored) {}
			throw new RuntimeException("CSVWriter.UnsupportedEncodingException (" + fileName + ")", e);
		}
	}
	
	public void writeRow(CSVRow row)
	{
        try
        {
			out.write(row.toCSVRow() + "\n");
		}
        catch (IOException e)
        {
        	throw new RuntimeException("CSVWriter.writeRow(CSVRow).IOException", e);
		}
	}
	
	public void writeRow(String row)
	{
        try
        {
			out.write(row + "\n");
		}
        catch (IOException e)
        {
        	throw new RuntimeException("CSVWriter.writeRow(String).IOException", e);
		}
	}
	
	public void writeRow(int n, CSVRow row)
	{
        try
        {
			out.write(n + ";" + row.toCSVRow() + "\n");
		}
        catch (IOException e)
        {
        	throw new RuntimeException("CSVWriter.writeRow(CSVRow).IOException", e);
		}
	}
	
	public void writeRow(int n, String row)
	{
        try
        {
			out.write(n + ";" + row + "\n");
		}
        catch (IOException e)
        {
        	throw new RuntimeException("CSVWriter.writeRow(String).IOException", e);
		}
	}
	
	public void close()
	{
		try { out.close(); } catch (IOException ioIgnored) {}
	}
}
























