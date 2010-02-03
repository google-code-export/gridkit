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
package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;

public class CSVHelper
{
	private static final String frenchSpace = Character.toString((char)160);
	private static final NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
	
	public static String formatDoubleToCSV(double d)
	{
		return format.format(d).replaceAll(frenchSpace, "");
	}
	
	static final class StatsCSVRow
	{
		static enum TimeMeasuringType
		{
			JavaMS, JavaNS, CoherenceMS
		}
		
		public final int executionOrder;
		public final CommandBenchmarkParams params;
		public final CommandBenchmarkStats.TimeUnitDependStats stats;
		public final TimeMeasuringType counterType;
		
		public StatsCSVRow(int executionOrder, CommandBenchmarkParams params, CommandBenchmarkStats.TimeUnitDependStats stats, TimeMeasuringType counterType)
		{
			this.executionOrder = executionOrder;
			this.params = params;
			this.stats = stats;
			this.counterType = counterType;
		}

		public String toCSVRow()
		{
			return executionOrder + ";" +
			    params.toCSVRow() + ";" +
			     stats.toCSVRow() + ";" +
		   counterType.toString();
		}
		
		public static String getCSVHeader()
		{
			return "ExecutionOrder;" + CommandBenchmarkParams.getCSVHeader() + ";" +
			CommandBenchmarkStats.TimeUnitDependStats.getCSVHeader() + ";" + "CounterType";
		}
	}
	
	public static boolean storeResultsInCSV(String fileName, Collection<StatsCSVRow> items)
	{
		File file = new File(fileName);
        
		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(file);
		}
		catch(FileNotFoundException e)
		{
			return false;
		}
		
        BufferedWriter out;
		try
		{
			out = new BufferedWriter(new OutputStreamWriter(fos,"ansi-1251"));
		}
		catch (UnsupportedEncodingException e)
		{
			try { fos.close(); } catch (IOException ioIgnored) {}
			return false;
		}
       
        try
        {
			out.write(StatsCSVRow.getCSVHeader() + "\n");
			
			for (StatsCSVRow r : items)
			{
				out.write(r.toCSVRow() + "\n");
			}
		}
        catch (IOException e)
        {
			return false;
		}
        finally
        {
        	try { out.close(); } catch (IOException ioIgnored) {}
        }
       
        return true;
	}
}
