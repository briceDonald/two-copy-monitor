import java.util.concurrent.atomic.AtomicStampedReference;


/**
 * @author Brice Ngnigha && Abed Haque
 * @param <T> the type to operate on
 */
public class TwoCopyMonitor<T> {

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
	public TwoCopyMonitor( T initialValue ) {

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
		swapReference();
	}

	
	/**
	 * @brief  Swaps the references of two AtomicStampedReferences
	 */
	private void swapReference() {
		T readerRef = reader.getReference();
		T writerRef = writer.getReference();
		
		if( readerRef.equals(writerRef) )
			return;
		
		writer.set(readerRef, WR_STAMP);
		reader.set(writerRef, RD_STAMP);
	}

}
