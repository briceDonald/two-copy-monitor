import java.security.PrivateKey;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 
 */

/**
 * @author Brice Ngnigha && Abed Haque
 * @param <T> the type to operate on
 */

public class TwoCopyMonitor<T> {

	private final int WR_EVENT = 0;
	private final int WR_STAMP = 1;
	private final int RD_STAMP = 2;
	
	// Atomic stamped references
	AtomicStampedReference<T> writer;
	AtomicStampedReference<T> reader;

	
	private AtomicBoolean cas_event;
	
	/**
	 * @brief TwoCopyMonitor constructor
	 * @param T initialValue, the initial value of the TwoCopyMonitor
	 */
	public TwoCopyMonitor( T initialValue ) {

		// Sets the initial value of the monitor
		T copyA = initialValue;
		T copyB = initialValue;
		
		// Init the atomic references
		reader = new AtomicStampedReference<T>(copyA, RD_STAMP);
		writer = new AtomicStampedReference<T>(copyB, WR_STAMP);
		
		cas_event = new AtomicBoolean(false);
	}
	
	/**
	 *  @brief  Allows access of the value only when no cav_event is on
	 *  @return T reader, returns the value of the monitor
	 *  
	 */
	public T get() {		
		
		T curRdRef = reader.getReference();
		
		while( !reader.compareAndSet(curRdRef, curRdRef, RD_STAMP, RD_STAMP) ) {
			curRdRef = reader.getReference();
		}

		return curRdRef;
	}
	
	public void set( T newVal) {
		
		T curRdRef = reader.getReference();
		
		// The reader has the WR_READ stamp, making it impossible to 
		// other threads to write or read the value
		while( !reader.compareAndSet(curRdRef, curRdRef, RD_STAMP, WR_EVENT) ) {
			curRdRef = reader.getReference();
			Thread.yield();
		}
		
		// set the writer to the new value
		writer.set(newVal, WR_EVENT);
		
		// Swap the reader and the writer references
		swapReference();
	}

	/**
	 * @brief  Swaps the references of two AtomicStampedReferences
	 */
	private void swapReference() {
		T readerRef = reader.getReference();
		T writerRef = writer.getReference();
		
		writer.set(readerRef, WR_STAMP);
		reader.set(writerRef, RD_STAMP);
	}

	/**
	 * @param Test single writer multiple readers
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Integer a;
		final TwoCopyMonitor<Integer> monitor = new TwoCopyMonitor<Integer>(15);
		System.out.println("TwoCopyMonitor");
		System.out.println("Monitor Value1: " + monitor.get() );
		monitor.set(12);
		System.out.println("Monitor Value2: " + monitor.get() );
		
		int numThreads = 10000;
		
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
