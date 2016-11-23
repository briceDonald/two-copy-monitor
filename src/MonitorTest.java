import static org.junit.Assert.*;
import org.junit.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by abed on 11/22/16.
 */
public class MonitorTest
{
    MonitorObj<Integer> monitor;

    @Before public void initialize()
    {
        monitor = new SingleCopyMonitor<>(5);
    }

    @Test
    public void multipleReadersReadCorrectlyAfterInit()
    {
        int numThreads = 100;
        for( int i = 0; i < numThreads; i++) {
            Thread readers = new Thread( new Runnable() {
                @Override
                public void run() {
                    assertEquals(5, (int)monitor.get());
                }
            }
            );

            readers.start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void multipleReadersReadCorrectlyAfterWrite()
    {
        AtomicInteger v = new AtomicInteger(10000);

        // Single Writer
        Thread writer = new Thread( new Runnable() {
            @Override
            public void run() {
                do
                {
                    monitor.set(v.decrementAndGet());
                }while (v.get() > 0);
            }
        }
        );

        writer.start();

        // Multiple Readers
        int numThreads = 100;
        for( int i = 0; i < numThreads; i++) {
            Thread readers = new Thread( new Runnable() {
                @Override
                public void run() {
                    //System.out.println(Thread.currentThread().getId() + " Monitor:" + monitor.get() );
                    assertEquals(v.get(), (int)monitor.get());
                }
            }
            );

            readers.start();
        }

        try {
            writer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}