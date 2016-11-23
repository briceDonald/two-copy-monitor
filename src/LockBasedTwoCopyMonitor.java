import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 */

/**
 * @author Brice Ngnigha && Abed Haque
 * @param <T> the type to operate on
 */

public class LockBasedTwoCopyMonitor<T> {

	private final int WR_EVENT = 0;
	private final int WR_STAMP = 1;
	private final int RD_STAMP = 2;
	
	private T copyA;
	private T copyB;
	
	// Atomic stamped references
	private T writer;
	private T reader;
	
	// write event lock
	Lock wLock;
	Condition inWrite;

	
	private AtomicBoolean write_event;
	
	/**
	 * @brief TwoCopyMonitor constructor
	 * @param T initialValue, the initial value of the TwoCopyMonitor
	 */
	public LockBasedTwoCopyMonitor( T initialValue ) {

		// Sets the initial value of the monitor
		copyA = initialValue;
		copyB = initialValue;
		
		// Init the atomic references
		reader = copyA;
		writer = copyB;
		
		// write event lock
		wLock = new ReentrantLock();
		inWrite = wLock.newCondition();
		
		write_event = new AtomicBoolean(false);
	}
	
	/**
	 *  @brief  Allows access of the value only when no cav_event is on
	 *  @return T reader, returns the value of the monitor
	 * @throws InterruptedException 
	 *  
	 */
	public T get() {		
		
		T curRdRef = null;
		System.out.println("-");
		
		if( write_event.get() )
		{
//			wLock.lock();
//			try {
//				System.out.println(".");
//				inWrite.await();
//				System.out.println(".-");
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			curRdRef = reader;
			System.out.println("read A");
//			wLock.unlock();
		}
		else
		{
			curRdRef = reader;
			System.out.println("read B");
		}

		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return curRdRef;
	}
	
	public void set( T newVal) {
		
		wLock.lock();
		
		write_event.set(true);
		
//		System.out.println("1 copies A " + copyA + " B " + copyB);
		writer = newVal;
//		System.out.println("2 copies A " + copyA + " B " + copyB);
		// set the writer to the new value
		
		write_event.set(true);
		
		// Swap the reader and the writer references
		T tempReader = reader;
		reader = writer;
		writer = tempReader;
		
		System.out.println("3 copies A " + reader + " B " + writer);
		
		inWrite.signalAll();
		wLock.unlock();
	}

	/**
	 * @param Test single writer multiple readers
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Integer a;
		final LockBasedTwoCopyMonitor<Integer> monitor = new LockBasedTwoCopyMonitor<Integer>(15);
		System.out.println("TwoCopyMonitor");
		System.out.println("Monitor Value1: " + monitor.get() );
		monitor.set(12);
		System.out.println("Monitor Value2: " + monitor.get() );
		
		int numThreads = 100;
		
		////////////////////////// Single writer
		Thread writer = new Thread( new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						int v = 200;
						while( v > 0)
							monitor.set(v--);
					}
				}
			);
		writer.start();
		
		///////////////////////// Multiple reader
		for( int i = 0; i < numThreads; i++) {
			Thread readers = new Thread( new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					System.out.println(Thread.currentThread().getId() + " Monitor:" + monitor.get() );
					}
				}
			);
		 
			readers.start();
		}
		
		try {
			writer.join();
			System.out.println("Done");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
