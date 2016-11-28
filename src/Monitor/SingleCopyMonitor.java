package Monitor;
/**
 * @author Brice Ngnigha && Abed Haque
 * @param <T> the type to operate on
 */

public class SingleCopyMonitor<T> implements MonitorObj<T>
{
    T value;
    long waitTime;
    String type = "singleCopyMonitor";
    
    public String getType()
    {
    	return type;
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




	public synchronized T testGet( TimestampedInt readTime ) {
		readTime.timestamp = System.nanoTime();
		T val = value;
		if(waitTime > 0)
    		timedExecution(waitTime);
        return val;
	}

	public synchronized void testSet(T newVal, TimestampedInt writeStamp) {
		// TODO Auto-generated method stub
		value = newVal;
		writeStamp.timestamp = System.nanoTime();
		if(waitTime > 0)
    		timedExecution(waitTime);
	}
}
