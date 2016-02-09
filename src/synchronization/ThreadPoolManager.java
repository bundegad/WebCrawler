package synchronization;

import java.util.HashMap;

public class ThreadPoolManager {
	
	public interface IOnEmptyCallback {
		void onEmpty();
	}

	private static ThreadPoolManager manager;
	
	HashMap<String, ThreadPool<Runnable>> pools;
	
	public static ThreadPoolManager getInstance() {
		
		if (manager == null) {
			manager = new ThreadPoolManager();
		}
		
		return manager;
	}
	
	private ThreadPoolManager() {
		this.pools = new HashMap<>();
	}
	
	
	public void add(ThreadPool<Runnable> pool, String name) {
		pools.put(name, pool);
	}
	
	public ThreadPool<Runnable> get(String name) {
		return pools.get(name);
	}
	
	public void bind(String[] names, IOnEmptyCallback callback) {
		
		PoolLatchCounter latch = new PoolLatchCounter(callback);
		for (String name : names) {
			ThreadPool<Runnable> pool = pools.get(name);
			pool.setLatch(latch);
		}
	}
	
	
	public static class PoolLatchCounter  {
		
		int counter;
		IOnEmptyCallback callback;
		
		public PoolLatchCounter(IOnEmptyCallback callback) {
			counter = 0;
			this.callback = callback;
		}
		
		public synchronized void up() {
			counter++;
		}
		
		public synchronized void down() {
			if (counter > 0) {
				counter--;
				if (counter == 0 && callback != null) {
					callback.onEmpty();
				}
			}
		}
		
	}
}
