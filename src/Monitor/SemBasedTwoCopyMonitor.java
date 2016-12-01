package Monitor;
import java.util.concurrent.Semaphore;

/**
 * @author Brice Ngnigha && Abed Haque
 * @param <T> the type to operate on
 */

public class SemBasedTwoCopyMonitor<T> implements MonitorObj<T> {
	
	private T copyA;
	private T copyB;
	
	// Atomic stamped references
	private T writer;
	private T reader;
	
	// write event lock
	Semaphore writeSem;
	
	// waiting time
	long wait;

	
	/**
	 * @brief TwoCopyMonitor constructor
	 * @param T initialValue, the initial value of the TwoCopyMonitor
	 */
	public SemBasedTwoCopyMonitor( T initialValue ) {
		// assign initial values to the two copies
		copyA = initialValue;
		copyB = initialValue;
		
		// the atomic references
		reader = copyA;
		writer = copyB;
		
		// write event lock
		writeSem = new Semaphore(1);
	}
	
	/**
	 * @brief TwoCopyMonitor constructor
	 * @param T initialValue, the initial value of the TwoCopyMonitor
	 */
	public SemBasedTwoCopyMonitor( T initialValue, long waitTime ) {
		wait = waitTime;
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
		T curRdRef = reader;;
		
		while( writeSem.availablePermits() == 0 )
		{
			Thread.yield();
			curRdRef = reader;
		}
		
		if(wait > 0)
    		timedExecution(wait);
		
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
		} catch (InterruptedException e) { }

		writer = newVal;
		
		// Swap the reader and the writer references
		T tempReader = reader;
		reader = writer;
		writer = tempReader;
		
		if(wait > 0)
    		timedExecution(wait);
				
		writeSem.release();
	}
	
	/**
	 * 	@brief 	Test helper function to force the setter or getter to wait
	 *  @param  time, the time in milliseconds to wait
	 * 
	 */
	private void timedExecution( long time )
	{
		try
		{
			Thread.sleep(time);
		}
		catch (InterruptedException e)
		{
		}
	}
	
	
	/**
	 * 	@brief 	Returns the string type of this object
	 * 
	 */
	public String getType() {
		// TODO Auto-generated method stub
		return "SemBasedTwoCopyMonitor";
	}

}
