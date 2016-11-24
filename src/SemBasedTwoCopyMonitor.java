import java.util.concurrent.Semaphore;

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
		
	}
	
	
	/**
	 *  @brief  Reads only when no other write holds the semaphore
	 *  @return T reader, returns the value of the monitor
	 *  
	 */
	public T get() {		
		
		T curRdRef = reader;
		
		while( writeSem.availablePermits() == 0 )
		{
			Thread.yield();
			curRdRef = reader;
		}

		return curRdRef;
	}
	
	/**
	 *  @brief  Acquires the semaphore then writes
	 *  @return T reader, returns the value of the monitor
	 *  
	 */
	public void set( T newVal) {
		
		try {
			writeSem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		writer = newVal;
		
		// Swap the reader and the writer references
		T tempReader = reader;
		reader = writer;
		writer = tempReader;
				
		writeSem.release();
	}

}
