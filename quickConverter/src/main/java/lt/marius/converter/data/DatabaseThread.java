package lt.marius.converter.data;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lt.marius.converter.utils.DatabaseUtils;

public class DatabaseThread {
	
	private static DatabaseThread INSTANCE = new DatabaseThread();
	
	public static DatabaseThread getInstance() {
		return INSTANCE;
	}
	
	private ExecutorService exec = null;
	
	void init() {
		if (exec != null) {
			exec.shutdownNow();
		}
		exec = Executors.newSingleThreadExecutor();
	}
	
	boolean isAlive() {
		return exec != null;
	}
	
	public void submit(Runnable run) {
		if (exec != null && !exec.isShutdown()) {
			exec.execute(run);
		}
	}
	
	private Runnable closeRunnable = new Runnable() {
		
		@Override
		public void run() {
			DatabaseUtils.closeDatabase();
			exec = null;
		}
	};
	
	public void shutdown() {
		submit(closeRunnable);
		exec.shutdown();
	}
	
	public void shutdownNow() {
		exec.shutdownNow();
		closeRunnable.run();
	}
	
}
