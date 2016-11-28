package Monitor;
/**
 * @author Brice Ngnigha && Abed Haque
 * @param <T> the type to operate on
 */

public interface MonitorObj<T>
{
	T get();
    void set (T newVal);
    
    T testGet( TimestampedInt readtime );
    void testSet(T newVal, TimestampedInt writeTime);
}
