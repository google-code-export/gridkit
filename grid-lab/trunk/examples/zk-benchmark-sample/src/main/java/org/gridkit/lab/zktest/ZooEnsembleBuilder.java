package org.gridkit.lab.zktest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.server.quorum.QuorumPeer;
import org.apache.zookeeper.server.quorum.QuorumPeer.ServerState;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.gridkit.nimble.driver.Activity;

public class ZooEnsembleBuilder {

	public static ZooEnsembleBuilder build() {
		return new ZooEnsembleBuilder();
	}
	
	Map<String, String> zooProps = new HashMap<String, String>();
	String baseDataDir = ".zookeeper";
	int basePort = 30000;
	
	public ZooEnsembleBuilder() {
		maxClientsCnxns(0);
		tickTime(2000);
		minSessionTimeout(30000);
		maxSessionTimeout(60000);
		initLimit(10);
		syncLimit(5);
	}

	public ZooEnsembleBuilder maxClientsCnxns(int n) {
		zooProps.put("maxClientCnxns", String.valueOf(n));
		return this;
	}

	public ZooEnsembleBuilder tickTime(int ms) {
		zooProps.put("tickTime", String.valueOf(ms));
		return this;
	}

	public ZooEnsembleBuilder minSessionTimeout(int ms) {
		zooProps.put("minSessionTimeout", String.valueOf(ms));
		return this;
	}

	public ZooEnsembleBuilder maxSessionTimeout(int ms) {
		zooProps.put("maxSessionTimeout", String.valueOf(ms));
		return this;
	}

	public ZooEnsembleBuilder initLimit(int ticks) {
		zooProps.put("initLimit", String.valueOf(ticks));
		return this;
	}

	public ZooEnsembleBuilder syncLimit(int ticks) {
		zooProps.put("syncLimit", String.valueOf(ticks));
		return this;
	}

	public ZooEnsembleDriver driver() {
		Driver driver = new Driver();
		driver.zooProps = new HashMap<String, String>(zooProps);

		driver.baseDataDir = baseDataDir;
		driver.basePort = basePort;
		return driver;
	}
	
	public interface ZooEnsembleDriver {
		
		public void setBaseDataDir(String path);

		public void init();
		
		public void cleanData();
		
		public void publishURI(DataSink<String> uriSink);
		
		public Activity start();
		
	}
	
	private static class Driver implements ZooEnsembleDriver, Serializable {

		private static final long serialVersionUID = 20131116L;
		
		Map<String, String> zooProps = new HashMap<String, String>();
		String baseDataDir = ".zookeeper";
		int basePort = 30000;

		transient String uid;
		Reducer<MemberDigest, Collection<MemberDigest>> reducer = Reducers.collect();
		List<MemberDigest> ensemble;
		int myId = -1;
		MemberDigest me;
		
		boolean initialized;
		boolean configured;

		
		
		@Override
		public synchronized void setBaseDataDir(String path) {
			baseDataDir = path;
		}
		
		@Override
		public synchronized void init() {
			try {
				if (initialized) {
					throw new IllegalStateException("Already initialized");
				}				
				uid = new UID().toString();
				
				MemberDigest d = new MemberDigest();
				d.hostname = InetAddress.getLocalHost().getHostName();
				d.nodename = System.getProperty("vinode.name", "");
				d.uid = uid; 
				initialized = true; 
				reducer.add(d);
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void cleanData() {
			try {
				File f = new File(FSHelper.normalizePath(baseDataDir));
				FSHelper.removeDir(f);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		private void configure() {
			List<MemberDigest> ensemble = new ArrayList<MemberDigest>(reducer.reduce());
			sort(ensemble);
			String lh = null;
			int nl = 0;
			for(int i = 0; i != ensemble.size(); ++i) {
				ensemble.get(i).zooId = i + 1;
				if (ensemble.get(i).hostname.equals(lh)) {
					ensemble.get(i).localNo = nl++;
				}
				else {
					ensemble.get(i).localNo = 0;
					nl = 1;
					lh = ensemble.get(i).hostname;					
				}
				if (uid.equals(ensemble.get(i).uid)) {
					myId = ensemble.get(i).zooId;
					me = ensemble.get(i);
				}
			}
			this.ensemble = ensemble;
			for (MemberDigest zmi : ensemble) {
				String row = zmi.hostname + ":" + zmi.quorumPort(basePort) + ":" + zmi.leaderPort(basePort);
				zooProps.put("server." + zmi.zooId, row);
			}
		}
		
		private void sort(List<MemberDigest> ensemble) {
			Collections.sort(ensemble, new Comparator<MemberDigest>() {
				@Override
				public int compare(MemberDigest o1, MemberDigest o2) {					
					return (o1.hostname + " " + o1.nodename + " " + o1.uid).compareTo(o2.hostname + " " + o2.nodename + " " + o2.uid);
				}
			});			
		}

		private synchronized void ensureConfigured() {
			if (!initialized) {
				throw new IllegalStateException("Should call init first");				
			}
			if (!configured) {
				configure();
				configured = true;
			}
		}

		@Override
		public void publishURI(DataSink<String> uriSink) {
			ensureConfigured();
			
			StringBuilder sb = new StringBuilder();
			for(MemberDigest md: ensemble) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(md.hostname).append(":").append(md.clientPort(basePort));
			}
			uriSink.push(sb.toString());
		}

		@Override
		public Activity start() {
			ensureConfigured();

			try {
				String dataDir = new File(new File(FSHelper.normalizePath(baseDataDir)), "node-" + myId).getCanonicalPath();
				
				Properties props = new Properties();
				props.putAll(zooProps);
				props.put("dataDir", dataDir);
				props.put("clientPort", me.clientPort(basePort));

				File dir = new File(dataDir);
				dir.mkdirs();
				new File(dir, "version-2").mkdirs();			
				FileWriter fw = new FileWriter(new File(dir, "myid"));
				fw.append(String.valueOf(myId));
				fw.close();
				
				QuorumPeerConfig qpc = new QuorumPeerConfig();
				qpc.parseProperties(props);
				
				return startPeer(qpc);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		private Activity startPeer(QuorumPeerConfig config) throws IOException, InterruptedException {
			System.out.println("Starting quorum peer");

			QuorumPeer quorumPeer;
			ServerCnxnFactory cnxnFactory = ServerCnxnFactory.createFactory();
			cnxnFactory.configure(config.getClientPortAddress(),
					config.getMaxClientCnxns());

			quorumPeer = new QuorumPeer();
			quorumPeer.setClientPortAddress(config.getClientPortAddress());
			quorumPeer.setTxnFactory(
					new FileTxnSnapLog(
							new File(config.getDataLogDir()), 
							new File(config.getDataDir())
					));
			quorumPeer.setQuorumPeers(config.getServers());
			quorumPeer.setElectionType(config.getElectionAlg());
			quorumPeer.setMyid(config.getServerId());
			quorumPeer.setTickTime(config.getTickTime());
			quorumPeer.setMinSessionTimeout(config.getMinSessionTimeout());
			quorumPeer.setMaxSessionTimeout(config.getMaxSessionTimeout());
			quorumPeer.setInitLimit(config.getInitLimit());
			quorumPeer.setSyncLimit(config.getSyncLimit());
			quorumPeer.setQuorumVerifier(config.getQuorumVerifier());
			quorumPeer.setCnxnFactory(cnxnFactory);
			quorumPeer.setZKDatabase(new ZKDatabase(quorumPeer.getTxnFactory()));
			quorumPeer.setLearnerType(config.getPeerType());

			quorumPeer.start();
			System.out.println("Quorum peer started, looking for quorum");
			while(quorumPeer.getPeerState() == ServerState.LOOKING) {
				Thread.sleep(100);
			}
			System.out.println("Peer state: " + quorumPeer.getServerState());
			return new ZooQuorumMember(quorumPeer);
		}
	}
	
	private static class ZooQuorumMember implements Activity {

		final QuorumPeer peer;
		
		public ZooQuorumMember(QuorumPeer peer) {
			this.peer = peer;
		}

		@Override
		public void stop() {
			peer.shutdown();
		}

		@Override
		public void join() {
			try {
				peer.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
	private static class MemberDigest implements Serializable {

		private static final long serialVersionUID = 20131116L;
		
		String uid;
		String nodename;
		String hostname;
		int zooId;
		int localNo;
		
		public int clientPort(int base) {
			return base + 10 * localNo; 
		}

		public int leaderPort(int base) {
			return base + 10 * localNo + 1; 
		}

		public int quorumPort(int base) {
			return base + 10 * localNo + 2; 
		}		
	}
	
	
}
