/**
 * @author Brice Ngnigha && Abed Haque
 * @param <T> the type to operate on
 */

public class SingleCopyMonitor<T> implements MonitorObj<T>
{
    T value;

    public SingleCopyMonitor(T initialValue)
    {
        value = initialValue;
    }

    public synchronized T get(){
        return value;
    }

    public synchronized void set(T newVal){
//        try
//        {
//            Thread.sleep(1);
//        } catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
        value = newVal;
    }
}
