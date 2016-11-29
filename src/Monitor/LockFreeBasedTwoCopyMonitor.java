package Monitor;

import java.util.concurrent.atomic.AtomicStampedReference;


/**
 * @author Brice Ngnigha && Abed Haque
 * @param <T> the type to operate on
 */
public class LockFreeBasedTwoCopyMonitor<T> implements MonitorObj<T> {

	private final int WR_EVENT = 0;
	private final int WR_STAMP = 1;
	private final int RD_STAMP = 2;
	
	// Atomic stamped references
	private AtomicStampedReference<T> writer;
	private AtomicStampedReference<T> reader;

	
	/**
	 * @brief TwoCopyMonitor constructor
	 * @param T initialValue, the initial value of the TwoCopyMonitor
	 */
	public LockFreeBasedTwoCopyMonitor( T initialValue, long waitT ) {

		// For test only
		waitTime = waitT;
				
		// Sets the initial value of the monitor
		T copyA = initialValue;
		T copyB = initialValue;
		
		reader = new AtomicStampedReference<T>(copyA, RD_STAMP);
		writer = new AtomicStampedReference<T>(copyB, WR_STAMP);
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
			Thread.yield();
		}

		return curRdRef;
	}
	
	
	/**
	 *  @brief  Exclusively sets the value of the monitor
	 *  @param  newVal, the new value to set the monitor
	 */
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
		T curWrRef = writer.getReference();
		writer.set(reader.getReference(), WR_STAMP);
		reader.set(curWrRef, RD_STAMP);
	}

	
	/******************************** test implementation ********************************/
	long waitTime;
    
    public String getType()
    {
    	return "lockFreeBasedTwoCopyMonitor";
    }
    

    private void timedExecution( long time )
    {
    	try
    	{
			Thread.sleep(time);
		}
    	catch (InterruptedException e)
		{
			e.printStackTrace();
		}
    }
    
    
	public  T testGet( TimestampedInt readTime ) {

		T curRdRef = reader.getReference();
		
		while( !reader.compareAndSet(curRdRef, curRdRef, RD_STAMP, RD_STAMP) ) {
			curRdRef = reader.getReference();
			Thread.yield();
		}

//		System.out.println("..");
		
		if(waitTime > 0)
    		timedExecution(waitTime);
		readTime.timestamp = System.nanoTime();
		return curRdRef;
	}
	

	public void testSet(T newVal, TimestampedInt writeStamp) {

		T curWrRef;
		T curRdRef = reader.getReference();

		// We must prevent threads from reading during a swap event, so stamp the
		// reader with WR_EVENT
		while( !reader.compareAndSet(curRdRef, curRdRef, RD_STAMP, WR_EVENT) ) {
			curRdRef = reader.getReference();
			Thread.yield();
		}

		writer.set(newVal, WR_EVENT);

		if(waitTime > 0)
    		timedExecution(waitTime);

		// Swap the reader and the writer references.
		curWrRef = writer.getReference();
		reader.set(curWrRef, RD_STAMP);
		writer.set(curRdRef, WR_STAMP);

		writeStamp.timestamp = System.nanoTime();
	}

}