package Monitor;
/**
 * @author Brice Ngnigha && Abed Haque
 * @param <T> the type to operate on
 */

public class SingleCopyMonitor<T> implements MonitorObj<T>
{
    T value;
    
    public SingleCopyMonitor(T initialValue, long waitT)
    {
        value = initialValue;
        waitTime = waitT;
    }

    public synchronized T get(){
    	T val = value;
    	if(waitTime > 0)
    		timedExecution(waitTime);
        return val;
    }

    public synchronized void set(T newVal){
        value = newVal;
        if(waitTime > 0)
    		timedExecution(waitTime);
    }


    // test data
    long waitTime;
    
    public String getType()
    {
    	return "singleCopyMonitor";
    }

    private void timedExecution( long time )
    {
    	try
    	{
			Thread.sleep(waitTime);
		}
    	catch (InterruptedException e)
		{
			e.printStackTrace();
		}
    }

	public synchronized T testGet( TimestampedInt readTime ) {
		
		T val = value;
		if(waitTime > 0)
    		timedExecution(waitTime);
		readTime.timestamp = System.nanoTime();
        return val;
	}

	public synchronized void testSet(T newVal, TimestampedInt writeStamp) {
		// TODO Auto-generated method stub
		value = newVal;
		
		if(waitTime > 0)
    		timedExecution(waitTime);
		writeStamp.timestamp = System.nanoTime();
	}
}
