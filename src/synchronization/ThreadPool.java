package synchronization;
import java.util.ArrayList;
import java.util.List;


public class ThreadPool<T extends Runnable>  {

	private int poolSize;
	private List<Thread> threadPool;
	private SyncQueue<T> executionQ;
	private ThreadPoolManager.PoolLatchCounter latch;
	

	public  ThreadPool(int poolSize) {
		//Initilise thread pool
		threadPool = new ArrayList<Thread>(poolSize);
		executionQ = new SyncQueue<>();
		this.poolSize = poolSize;
		createThreads();
	}
	
	public void execute(T command) {
		try {
			safeUp();
			executionQ.push(command);
		} catch (Exception e) {
			
		}
	}
	
	public void executeAll(T[] commands) {
		for (T t : commands) {
			execute(t);
		}
	}
	
	public synchronized void start() {
		
		executionQ.clear();
		 for (Thread thread : threadPool) {
			thread.start();
		}
	}
	
	
	public  synchronized void stop() {
		for (Thread thread : threadPool) {
			thread.interrupt();
		}
		
		threadPool.clear();
		executionQ.clear();
		
		createThreads();
	}
	
	
	
	public void setLatch(ThreadPoolManager.PoolLatchCounter latch) {
		this.latch = latch;
	}
	
	private  void safeUp() {
		if (latch != null) {
			latch.up();
		}
	}
	
	private void safeDown() {
		if (latch != null) {
			latch.down();
		}
	}
	
	private void createThreads() {
		for (int i = 0; i < poolSize; i++) {
			Thread t  = new Thread(new Runnable() {
				
				
				public void run() {
					
					while (!Thread.currentThread().isInterrupted()){
						
						Runnable r;
						
						try {
							r = executionQ.pull();
							
							
							if(r != null) {
								r.run();
								safeDown();
							}
							
							
						} catch (InterruptedException e) {
							return;
						} catch (Exception e) {
							continue;
						}
					}
				}
			});
			threadPool.add(t);
		}
	}

	public int getSize() {
		return poolSize;
	}
}
