package org.gridkit.fabric.remoting.hub;

import java.io.Closeable;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import org.gridkit.fabric.remoting.DuplexStream;
import org.gridkit.fabric.remoting.RmiGateway;
import org.gridkit.fabric.remoting.RmiGateway.StreamErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemotingHub {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(RemotingHub.class);

	private final static int UID_LENGTH = 32;
	
	private SecureRandom srnd ;
	
	private ConcurrentMap<String, ConnectionContext> connections = new ConcurrentHashMap<String, ConnectionContext>();
	
	public RemotingHub() {
		try {
			srnd = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String newConnection(ConnectionEventListener listener) {
		while(true) {
			String uid = generateUID();
			ConnectionContext ctx = new ConnectionContext();
			ctx.uid = uid;
			ctx.listener = listener;			
			synchronized(ctx) {
				if (connections.putIfAbsent(uid, ctx) != null) {
					continue;
				}
				ctx.gateway = new RmiGateway();
				ctx.gateway.setStreamErrorHandler(ctx);
			}
			return uid;
		}		
	}
	
	public ExecutorService getExecutionService(String uid) {
		ConnectionContext ctx = connections.get(uid);
		if (ctx != null) {
			return ctx.gateway.getRemoteExecutorService();
		}
		else {
			return null;
		}
	}
	
	private String generateUID() {
		byte[] magic = new byte[UID_LENGTH / 2];
		srnd.nextBytes(magic);
		StringBuilder sb = new StringBuilder();
		for(byte b:magic) {
			sb.append(Integer.toHexString(0xF & (b >> 4)));
			sb.append(Integer.toHexString(0xF & b));
		}
		return sb.toString();
	}

	public void closeConnection(String id) {
		ConnectionContext ctx = connections.get(id);
		if (ctx != null) {
			synchronized(ctx) {
				ctx = connections.get(id);
				if (ctx != null) {
					ctx.listener.closed();
					silentClose(ctx.stream);
					ctx.gateway.shutdown();
					connections.remove(id);
					
					// done
					return;
				}
			}
		}
		throw new IllegalArgumentException("Connection not found " + id);
	}
	
	public void dispatch(DuplexStream stream) {
		String id = readId(stream);
		if (id != null) {
			ConnectionContext ctx = connections.get(id);
			if (ctx != null) {
				synchronized(ctx) {
					ctx = connections.get(id);
					if (ctx != null) {
						if (ctx.stream != null) {
							silentClose(ctx.stream);
							ctx.gateway.disconnect();
							if (ctx.stream != null) {
								ctx.listener.interrupted(ctx.stream);
								ctx.stream = null;
							}
						}
						try {
							ctx.gateway.connect(stream);
							ctx.stream = stream;
							ctx.listener.connected(stream);
						} catch (IOException e) {
							LOGGER.error("Stream connection failed " + stream);
						}
						LOGGER.info("Stream connected at end point " + id + " - " + stream);
						return;
					}
				}
			}
		}
		LOGGER.warn("Stream were not connected " + stream);
		silentClose(stream);
	}
	
	private String readId(DuplexStream stream) {
		try {
			byte[] magic = new byte[UID_LENGTH];
			for(int i = 0; i != magic.length; ++i) {
				magic[i] = (byte) stream.getInput().read();
			}
			return new String(magic);
		} catch (IOException e) {
			return null;
		}
	}

	public interface ConnectionEventListener {
		
		public void connected(DuplexStream stream);
		
		public void interrupted(DuplexStream stream);
		
		public void reconnected(DuplexStream stream);
		
		public void closed();		
	}
	
	private class ConnectionContext implements StreamErrorHandler {
		
		private String uid;
		private ConnectionEventListener listener;
		private RmiGateway gateway;
		private DuplexStream stream;

		@Override
		public synchronized void streamError(DuplexStream socket, Object stream, Exception error) {
			gateway.disconnect();
			this.stream = null;
			listener.interrupted(socket);
		}
	}
	
	private static final void silentClose(Closeable ch) {
		try {
			ch.close();
		} catch (IOException e) {
			// ignore
		}
	}
}
