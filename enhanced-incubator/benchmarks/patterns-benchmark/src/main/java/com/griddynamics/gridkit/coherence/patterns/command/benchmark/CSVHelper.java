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
	private static String frenchSpace = Character.toString((char)160);
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
		public final BenchmarkStats stats;
		public final TimeMeasuringType counterType;
		
		public StatsCSVRow(int executionOrder, CommandBenchmarkParams params, BenchmarkStats stats, TimeMeasuringType counterType)
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
				   BenchmarkStats.getCSVHeader() + ";" + "CounterType";
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
