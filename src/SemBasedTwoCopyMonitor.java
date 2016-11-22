import java.util.concurrent.Semaphore;
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

public class SemBasedTwoCopyMonitor<T> {

	private final int WR_EVENT = 0;
	private final int WR_STAMP = 1;
	private final int RD_STAMP = 2;
	
	private T copyA;
	private T copyB;
	
	// Atomic stamped references
	private T writer;
	private T reader;
	
	// write event lock
	Semaphore writeSem;

	
	private AtomicBoolean write_event;
	
	/**
	 * @brief TwoCopyMonitor constructor
	 * @param T initialValue, the initial value of the TwoCopyMonitor
	 */
	public SemBasedTwoCopyMonitor( T initialValue ) {

		// Sets the initial value of the monitor
		copyA = initialValue;
		copyB = initialValue;
		
		// Init the atomic references
		reader = copyA;
		writer = copyB;
		
		// write event lock
		writeSem = new Semaphore(1);
		
		write_event = new AtomicBoolean(false);
	}
	
	/**
	 *  @brief  Allows access of the value only when no cav_event is on
	 *  @return T reader, returns the value of the monitor
	 *  
	 */
	public T get() {		
		
		T curRdRef = null;
		System.out.println(Thread.currentThread().getId() + "------ Monitor:" + reader );
		
		while( writeSem.availablePermits() == 0 )
		{
			System.out.println(".");
			Thread.yield();
		}

		return reader;
	}
	
	public void set( T newVal) {
		
		try {
			writeSem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		write_event.set(true);
		
		writer = newVal;

//		try {
//			Thread.sleep(1);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// Swap the reader and the writer references
		T tempReader = reader;
		reader = writer;
		writer = tempReader;
		
		System.out.println(Thread.currentThread().getId() + " copies A " + reader + " B " + writer);
		
		writeSem.release();
	}

	/**
	 * @param Test single writer multiple readers
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Integer a;
		final SemBasedTwoCopyMonitor<Integer> monitor = new SemBasedTwoCopyMonitor<Integer>(15);
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
					System.out.println(Thread.currentThread().getId() + "***** Monitor:" + monitor.get() );
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
