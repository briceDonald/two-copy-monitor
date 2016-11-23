import static org.junit.Assert.*;
import org.junit.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;


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
    public void singleReaderReadsCorrectlyAfterInit()
    {
        assertEquals(5, (int)monitor.get());
    }

    @Test
    public void multipleReadersSingleWriter()
    {
        runTest(1000, 1, 1000);
    }

    @Test
    public void multipleReadersMultipleWriters()
    {
        runTest(1000, 1000, 1000);
    }

    private void runTest(int initialWriteValue, int numWriterThreads, int numReaderThreads)
    {
        AtomicInteger val = new AtomicInteger(initialWriteValue);
        AtomicLong timestamp = new AtomicLong(0);

        ExecutorService threadPool = Executors.newCachedThreadPool();

        Future<Boolean>[] writerFutures =  new Future[numWriterThreads];
        Future<Boolean>[] readerFutures = new Future[numReaderThreads];

        Thread createWriterFutures = new Thread(new Runnable()
        {
            public void run()
            {
                for (int i = 0; i < numWriterThreads; i++)
                {
                    writerFutures[i] = (threadPool.submit(new Callable<Boolean>()
                    {
                        public Boolean call()
                        {
                            timestamp.set(System.nanoTime());
                            monitor.set(val.decrementAndGet());

                            try
                            {
                                Thread.sleep(1);
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    }));
                }
            }
        });


        Thread createReaderFutures = new Thread(new Runnable()
        {
            public void run()
            {
                for (int i = 0; i < numReaderThreads; i++)
                {
                    readerFutures[i] = threadPool.submit(new Callable<Boolean>()
                    {
                        public Boolean call()
                        {
                            Boolean success = true;
                            long readTime = System.nanoTime();
                            int actualValue = monitor.get();

                            int expectedValue = val.get();
                            long lastWriteTime = timestamp.get();

                            Boolean successCondition = (expectedValue == actualValue) && (readTime > lastWriteTime);
                            try
                            {
                                assertTrue(GenerateFailureMessage(expectedValue, actualValue, readTime, lastWriteTime), successCondition);
                            }
                            catch (AssertionError e)
                            {
                                System.out.println(e.getMessage());
                                success = false;
                            }

                            return success;
                        }
                    });
                }
            }
        });

        createWriterFutures.start();
        createReaderFutures.start();

        try
        {
            createWriterFutures.join();
            createReaderFutures.join();

            // Wait for all futures to complete

            for (int i = 0; i < numWriterThreads; i++)
                try
                {
                    writerFutures[i].get();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }

            for (int i = 0; i < numReaderThreads; i++)
                try
                {
                    if(!readerFutures[i].get())
                        fail();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }

        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private String GenerateFailureMessage(int expectedValue, int actualValue, long readTime, long lastWriteTime)
    {
        return "Expected " + expectedValue + ", got " + actualValue + " (Read occurred " + (readTime - lastWriteTime) + "ns after most recent write)";
    }
}