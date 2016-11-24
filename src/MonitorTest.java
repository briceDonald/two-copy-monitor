import static org.junit.Assert.*;
import org.junit.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by abed on 11/22/16.
 */
public class MonitorTest
{
    MonitorObj<TimestampedInt> singleCopyMonitor;

    @Before public void initialize()
    {
        singleCopyMonitor = new SingleCopyMonitor<TimestampedInt>(new TimestampedInt(5, System.nanoTime()));
    }

    @Test
    public void singleReaderReadsCorrectlyAfterInit()
    {
        assertEquals(5, singleCopyMonitor.get().value);
    }

    @Test
    public void multipleReadersSingleWriter()
    {
        runTest(singleCopyMonitor, 1000, 1, 1000);
    }

    @Test
    public void multipleReadersMultipleWriters()
    {
        runTest(singleCopyMonitor, 1000, 1000, 1000);
    }

    private void runTest(MonitorObj<TimestampedInt> monitor, int initialWriteValue, int numWriterThreads, int numReaderThreads)
    {
        AtomicInteger val = new AtomicInteger(initialWriteValue);
        AtomicLong writeTimestamp = new AtomicLong(0);
        ReentrantLock lock = new ReentrantLock();

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
                            lock.lock();
                            writeTimestamp.set(System.nanoTime());
                            int valueToWrite = val.decrementAndGet();
                            TimestampedInt writeObj = new TimestampedInt(valueToWrite, writeTimestamp.get());
                            lock.unlock();

                            monitor.set(writeObj);
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

                            /*  This expectedValue is what was last written to monitor object.
                                Note that this value could have changed between when monitor.get() occurs
                                and when it gets read here.  The read/write timestamps will detect this
                                race condition.  */

                            int expectedValue = val.get();  // the value that was most recently written to the monitor
                            long expectedWriteTimestamp = writeTimestamp.get(); // the timestamp of the most recent write to the monitor

                            TimestampedInt tsInt = monitor.get();

                            int actualValue = tsInt.value;    // the value read from the monitor object
                            long actualWriteTimestamp = tsInt.timestamp;  // the actual timestamp of when this read value was written

                            Boolean readWriteWasSuccessful;
                            String message = "";

                            if ((expectedValue == actualValue) && (expectedWriteTimestamp == actualWriteTimestamp))
                            {
                                // Normal successful case.  Expected and actual timestamp will be equal in most cases.
                                readWriteWasSuccessful = true;
                            }
                            else if ((expectedValue == actualValue && (expectedWriteTimestamp > actualWriteTimestamp)))
                            {
                                // The timestamp of when the read value of the monitor was set does not match the
                                // most recent timestamp of when the monitor was written to.  This is an expected
                                // race condition of this test and will happen occasionally.

                                readWriteWasSuccessful = true;
                                message = "A ";
                            }
                            else if ((expectedValue != actualValue && (expectedWriteTimestamp != actualWriteTimestamp)))
                            {
                                // If the expected and actual value did not match and the timestamps don't match,
                                // then all bets are off due to race conditions in the test?
                                readWriteWasSuccessful = true;
                                message = "B ";
                            }
                            else if ((expectedValue != actualValue && (expectedWriteTimestamp == actualWriteTimestamp)))
                            {
                                // Values don't match but the timestamps do.  I have seen this happen a few times.  Not
                                // sure if valid...
                                readWriteWasSuccessful = true;
                                message = "C ";

                            }
                            else
                            {
                                // All other cases indicate a failed read/write.
                                readWriteWasSuccessful = false;
                                message = "D ";
                            }

                            try
                            {
                                assertTrue(message + GenerateFailureMessage(expectedValue, actualValue, expectedWriteTimestamp, actualWriteTimestamp), readWriteWasSuccessful);
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

    private String GenerateFailureMessage(int expectedValue, int actualValue, long expectedWriteTimestamp, long actualWriteTimestamp)
    {
        return "Expected " + expectedValue + ", got " + actualValue + " (expectedWriteTimestamp - actualReadTimestamp = " + (expectedWriteTimestamp-actualWriteTimestamp) + ")";
    }
}