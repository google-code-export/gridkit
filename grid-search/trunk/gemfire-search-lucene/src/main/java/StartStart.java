

public class StartStart {
	
	public static void main(String[] args) {
		
		String[] startArgs = {
			"server",
//			"-server-port=50000",
//			"-log-file=cacheserver.log"
		};
		
//		CacheServerLauncher.main(startArgs);
		com.gemfire.workaround.CacheServerLauncher.main(startArgs);		
	}

}
