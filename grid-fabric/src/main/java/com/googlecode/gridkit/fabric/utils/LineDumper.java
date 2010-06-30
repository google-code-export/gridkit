package com.googlecode.gridkit.fabric.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Information need to start process.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LineDumper {
	
	private static final BlockingQueue<LineDumper> QUEUE = new LinkedBlockingQueue<LineDumper>();
	static {
		Thread thread = new Thread(){
			@Override
			public void run() {
				dumpLoop();
			}			
		};
		
		thread.setName(LineDumper.class.getName());
		thread.setDaemon(true);
		thread.start();
	}
	
	private final InputStream in;
	private final StringBuffer buffer = new StringBuffer();
	private final PrintStream out;
	private final String prefix;
	private boolean closed = false;
	
	public LineDumper(String prefix, InputStream in, PrintStream out) {
		this.prefix = prefix;
		this.in = in;
		this.out = out;
		QUEUE.add(this);
	}
	
	private void dump() {
		try {
			if (in.available() > 0) {
				byte[] data = new byte[512];
				int cn = in.read(data);
				if (cn < 0) {
					close(); 
					if (buffer.length() > 0) {
						buffer.append("\n");
					}
				}
				else if (cn > 0){
					buffer.append(new String(data, 0, cn));
				}
				dumpBuffer();
			}
		} catch (IOException e) {
			ioException(e);
			close();
		}
	}
	
	private void dumpBuffer() {
		while(true) {
			int n = buffer.indexOf("\n");
			if (n >= 0) {
				String line = buffer.substring(0, n);
				buffer.delete(0, n + 1);
				out.append(prefix + line + "\n");
			}
			else {
				break;
			}
		}
	}

	private void close() {
		try {
			closed = true;
			in.close();
		}
		catch(IOException e) {
			// ignore
		}
	}

	protected void ioException(IOException e) {
//		System.err.println(prefix + "exception!");
//		e.printStackTrace();
		// do nothing
	}

	private static void dumpLoop() {
		try {
			while(true) {
				LineDumper dumper = QUEUE.take();
				dumper.dump();
				if (!dumper.closed) {
					QUEUE.add(dumper);
				}
				Thread.sleep(1);
			}
		} catch (InterruptedException e) {
			// ignore
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
