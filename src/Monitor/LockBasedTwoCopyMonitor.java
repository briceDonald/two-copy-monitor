package Monitor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Monitor.TimestampedInt;

/**
 * @author 	Brice Ngnigha && Abed Haque
 * @param 	<T> the type to operate on
 * @brief	Implements a lock based two copy monitor
 * 			Uses one lock and one AtomicBoolean variable
 */

public class LockBasedTwoCopyMonitor<T> implements MonitorObj<T> {
	
	// pointers to data 
	private T writer;
	private T reader;
	
	// write event lock
	private Lock wLock;
	private AtomicBoolean write_event;
	
	
	/**
	 * @brief 	TwoCopyMonitor constructor
	 * @param 	T initialValue, the initial value of the LockBasedTwoCopyMonitor
	 */
	public LockBasedTwoCopyMonitor( T initialValue, long waitT ) {

		// For test only
		waitTime = waitT;
		
		// Sets the initial value of the monitor
		T copyA = initialValue;
		T copyB = initialValue;
		
		// Init the atomic references
		reader = copyA;
		writer = copyB;

		// write event lock
		wLock = new ReentrantLock();
		write_event = new AtomicBoolean(false);
	}
	
	
	/**
	 * @brief  	If no writing is in process, reads from the reader
	 *  		else , wait until the write completed then reads from the reader
	 * @return 	T reader, returns the value of the monitor
	 * @throws 	InterruptedException 
	 *  
	 */
	public T get() {		
		
		T curRdRef = reader;
		
		if( !write_event.get() )
			return curRdRef;
		
		synchronized(wLock) {
			try{
				while( write_event.get() )
				{
					wLock.wait();
					curRdRef = reader;
				}
				   
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			curRdRef = reader;
		}
		
		return curRdRef;
	}
	
	
	/**
	 * @brief  	Exclusively write the new value of the monitor
	 *  		inform all reader that a write operation is in progress
	 * @return 	T reader, returns the value of the monitor
	 *  
	 */
	public void set( T newVal) {
		
		synchronized(wLock) {
			
			write_event.set(true);
			
			// set the writer to the new value
			writer = newVal;
			
			// Swap the reader and the writer references
			T tempReader = reader;
			reader = writer;
			writer = tempReader;

			write_event.set(false);
			
			wLock.notify();
			
		}
	}
	
	
	/******************************** test implementation ********************************/
	long waitTime;
    
    public String getType()
    {
    	return "lockBasedTwoCopyMonitor";
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
	public T testGet( TimestampedInt readTime ) {
		
		T curRdRef = reader;
		
		if( !write_event.get() )
		{
			readTime.timestamp = System.nanoTime();
			if(waitTime > 0)
	    		timedExecution(waitTime);
			return curRdRef;
		}
		
		synchronized(wLock) {
			try{
				while( write_event.get() )
				{
					wLock.wait();
					curRdRef = reader;
				}
				   
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			curRdRef = reader;
			if(waitTime > 0)
	    		timedExecution(waitTime);
			readTime.timestamp = System.nanoTime();
		}
		
		return curRdRef;
	}

	public void testSet(T newVal, TimestampedInt writeStamp) {
		// set the writer to the new value
					
		synchronized(wLock) {
			
			write_event.set(true);
			
			writer = newVal;
			
			// Swap the reader and the writer references
			T tempReader = reader;
			reader = writer;
			writer = tempReader;
			
			if(waitTime > 0)
	    		timedExecution(waitTime);
			
			writeStamp.timestamp = System.nanoTime();
			write_event.set(false);
			wLock.notify();
		}
		
		
	}
	
}
