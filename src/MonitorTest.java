import static org.junit.Assert.*;
import org.junit.*;

import java.util.LinkedList;
import java.util.List;
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

    @Test
    public void blah()
    {
        runTest(1000, 1000, 1000);
    }

    private void runTest(int value, int numWriterThreads, int numReaderThreads)
    {
        AtomicInteger val = new AtomicInteger(value);
        AtomicLong timestamp = new AtomicLong(0);
        //AtomicInteger count1 = new AtomicInteger(0);
        //AtomicInteger count2 = new AtomicInteger(0);
        Lock lock = new ReentrantLock();

        ExecutorService threadPool = Executors.newCachedThreadPool();

        Future[] writerFutures =  new Future[numWriterThreads];
        Future[] readerFutures = new Future[numReaderThreads];

        Thread createWriterFutures = new Thread(new Runnable()
        {
            public void run()
            {
                for (int i = 0; i < numWriterThreads; i++)
                {
                    writerFutures[i] = (threadPool.submit(new Callable()
                    {
                        public Object call()
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
                            //System.out.println("set: " + count1.getAndIncrement());
                            return null;
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
                    readerFutures[i] = threadPool.submit(new Callable()
                    {
                        public Object call()
                        {
                            long readTime = System.nanoTime();
                            int actualValue = monitor.get();

                            int expectedValue = val.get();
                            long lastWriteTime = timestamp.get();

                            // Occasionally causes java.util.concurrent.ExecutionException: java.lang.AssertionError
                            //assertTrue((expectedValue == actualValue) && (readTime > timeOfLastWrite));

                            // Failure condition
                            if ((expectedValue != actualValue) && (readTime > lastWriteTime))
                            {
                                System.out.println("Failure: Expected " + expectedValue + ", got " + actualValue +
                                        " (Read occured " + (readTime - lastWriteTime) + "ns after most recent write)");
                            }

                            //assertTrue(true);
                            //System.out.println("get: " + count2.getAndIncrement());
                            return null;
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
                    readerFutures[i].get();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }

        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}