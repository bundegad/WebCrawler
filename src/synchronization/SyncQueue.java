package synchronization;
import java.util.LinkedList;
import java.util.Queue;


public class SyncQueue<T> {

    private final Queue<T> runnablesQueue;

    //Construct a Queue with initilised size.
    public SyncQueue() {
    	runnablesQueue = new LinkedList<T>();
    }
    
    //Push Runnable to the Queue
	public synchronized void push(T run) throws InterruptedException {
		this.runnablesQueue.add(run);
		notifyAll();
    }
    
	//Retrieve a Runnable from the Queue.
    public synchronized T pull() throws InterruptedException  {

    	while (runnablesQueue.isEmpty()) {
            wait();
        }

       return runnablesQueue.remove();     
    }
    
    public synchronized void clear() {
    	runnablesQueue.clear();
    }
}