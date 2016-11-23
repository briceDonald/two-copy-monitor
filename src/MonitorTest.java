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
        runTest(10000, 1, 1000);
    }

    private void runTest(int value, int numWriterThreads, int numReaderThreads)
    {
        AtomicInteger val = new AtomicInteger(value);
        AtomicLong timestamp = new AtomicLong(0);
        Lock lock = new ReentrantLock();

        ExecutorService threadPoolWriter = Executors.newCachedThreadPool();
        ExecutorService threadPoolReader = Executors.newCachedThreadPool();

        List<Future> writerFutures = new LinkedList<Future>();
        List<Future> readerFutures = new LinkedList<Future>();



        Thread createWriterFutures = new Thread(new Runnable()
        {
            public void run()
            {
                for (int i = 0; i < numWriterThreads; i++)
                {
                    writerFutures.add(threadPoolWriter.submit(new Callable()
                    {
                        public Object call()
                        {
                            monitor.set(val.decrementAndGet());
                            timestamp.set(System.nanoTime());
                            try
                            {
                                Thread.sleep(1);
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }

                            assertTrue(false);
                            //System.out.println("set" + val.toString());
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
                    readerFutures.add(threadPoolReader.submit(new Callable()
                    {
                        public Object call()
                        {
                            System.out.println("asdfasdfa");

                            //assertTrue(val.equals(monitor.get()) & System.nanoTime() > timestamp.get());
                            System.out.println("get" + monitor.get());
                            return null;
                        }
                    }));
                }
            }
        });

        createWriterFutures.start();
        createReaderFutures.start();

//        for (int i = 0; i < numReaderThreads; i++)
//        {
//            writerThreads.add(new Thread(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    monitor.get();
//                }
//            }
//            ));
//        }

//        // n Writers
//        for( int i = 0; i < numWriterThreads; i++)
//        {
//            Thread writer = new Thread(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    do
//                    {
//                        monitor.set(v.decrementAndGet());
//                    } while (v.get() > 0);
//                }
//            }
//            );
//        }
//
//        writer.start();
//
//        // Multiple Readers
//        for( int i = 0; i < numReaderThreads; i++) {
//            Thread readers = new Thread( new Runnable() {
//                @Override
//                public void run() {
//                    //System.out.println(Thread.currentThread().getId() + " Monitor:" + monitor.get() );
//                    assertEquals(v.get(), (int)monitor.get());
//                }
//            }
//            );
//
//            readers.start();
//        }
//
//        try {
//            writer.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}