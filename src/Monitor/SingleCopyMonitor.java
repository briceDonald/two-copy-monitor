package Monitor;
/**
 * @author Brice Ngnigha && Abed Haque
 * @param <T> the type to operate on
 */

public class SingleCopyMonitor<T> implements MonitorObj<T>
{
    T value;
    long wait;
    
    public SingleCopyMonitor(T initialValue, long waitTime)
    {
        value = initialValue;
        wait = waitTime;
    }
    
    public SingleCopyMonitor(T initialValue)
    {
        value = initialValue;
    }

    public synchronized T get(){
    	T val = value;
    	if(wait > 0)
    		timedExecution(wait);
        return val;
    }

    public synchronized void set(T newVal){
        value = newVal;
        if(wait > 0)
    		timedExecution(wait);
    }
    
    public String getType()
    {
    	return "SingleCopyMonitor";
    }

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
}
