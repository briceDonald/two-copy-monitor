package test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


import org.junit.*;
import static org.junit.Assert.*;



import Monitor.LockBasedTwoCopyMonitor;
import Monitor.LockFreeBasedTwoCopyMonitor;
import Monitor.MonitorObj;
import Monitor.SemBasedTwoCopyMonitor;
import Monitor.SingleCopyMonitor;
import Monitor.TimestampedInt;

//package test;


/**
 * Created by abed on 11/22/16.
 */
public class MonitorTest
{
	MonitorObj<Integer> singleCopyMonitor;
	MonitorObj<Integer> lockBasedCopyMonitor;
	MonitorObj<Integer> semBasedCopyMonitor;
	MonitorObj<Integer> lockFreeBasedCopyMonitor;

    @Before public void initialize()
    {
    	singleCopyMonitor = new SingleCopyMonitor<Integer>(5, 0);
    	lockBasedCopyMonitor = new LockBasedTwoCopyMonitor<Integer>(5, 0);
    	semBasedCopyMonitor = new SemBasedTwoCopyMonitor<Integer>(5, 0);
    	lockFreeBasedCopyMonitor = new LockFreeBasedTwoCopyMonitor<Integer>(5, 0);
    	
    }

    
    public void multipleReadersMultipleWriters( MonitorObj<Integer> monitor, PrintWriter writer) throws InterruptedException
    {
    	System.out.println(monitor.getType());
    	for(int i = 0; i < 80; i++)
    	{
    		int v =  (int) Math.pow(2, ((double)i)/5);
    		writer.println( runTest(monitor, v, v, v) );
    	}
    }


    private String runTest( final MonitorObj<Integer> monitor, final int initialWriteValue, 
    					  final int numWriterThreads, final int numReaderThreads)
    {
    	final TestData testData = new TestData();
    	
    	final Map<Integer, Long> map = Collections.synchronizedMap( new HashMap<Integer, Long>() );

    	map.put( monitor.get(), System.nanoTime() );
    	    	
        final Future<Boolean>[] writerFutures = new Future[numWriterThreads];
        final Future<Boolean>[] readerFutures = new Future[numReaderThreads];

        Thread createWriterFutures = new Thread(new Runnable()
        {
            public void run()
            {
            	final AtomicInteger value = new AtomicInteger(initialWriteValue);
            	ExecutorService writersPool = Executors.newCachedThreadPool();

                for (int i = 0; i < numWriterThreads; i++)
                {
                    writerFutures[i] = (writersPool.submit(new Callable<Boolean>()
                    {
                        public Boolean call()
                        {
                        	int val;
                        	long writeStartTime;
                        	TimestampedInt timeWritten = new TimestampedInt(0);
                        	
                        	val = value.decrementAndGet();
                        	
                        	writeStartTime = System.nanoTime();
                            monitor.testSet( val, timeWritten );
                            map.put(val, timeWritten.timestamp);
                            
                            testData.newWrite( timeWritten.timestamp - writeStartTime );
                            
                            return true;
                        }
                    }));
                }
                
                writersPool.shutdown();
            	try {
            		writersPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        });


        Thread createReaderFutures = new Thread(new Runnable()
        {
            public void run()
            {
            	ExecutorService readersPool = Executors.newCachedThreadPool();
            	
                for (int i = 0; i < numReaderThreads; i++)
                {
                    readerFutures[i] = readersPool.submit(new Callable<Boolean>()
                    {
                        public Boolean call()
                        {
                        	TimestampedInt timeRead = new TimestampedInt(0);
                        	long writeTime;
                        	int readValue;
                        	long readStartTime;
                            boolean success = true;

                            readStartTime = System.nanoTime();
                            readValue = monitor.testGet(timeRead);
                            testData.newRead( timeRead.timestamp - readStartTime );
                            
                            // wait until the value is written to the map
                            while(!map.containsKey(readValue));
                            writeTime = map.get(readValue);
                            
                            if( timeRead.timestamp < writeTime )
                            {
                            	System.out.println("**8** " + (timeRead.timestamp-writeTime) + " " + readValue );
                            	success = false;
                            }
                            
                            return success;
                        }
                    });
                }
                
                readersPool.shutdown();
            	try {
            		readersPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        });

        createWriterFutures.start();
        createReaderFutures.start();

        try
        {
        	// Wait for all futures to complete
            createWriterFutures.join();
            createReaderFutures.join();            

            for (int i = 0; i < numWriterThreads; i++)
            {
                try
                {
                    writerFutures[i].get();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < numReaderThreads; i++)
            {
                try
                {
                	assertTrue( readerFutures[i].get() );
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
            }

        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        return testData.getResults();
    }    


    @Test
    public void testMonitors() throws InterruptedException, FileNotFoundException
    {
    	PrintWriter writer;

        writer = new PrintWriter( lockFreeBasedCopyMonitor.getType() + ".csv" );
        writer.println( "Readers, AvgReadTime, Writers, AvgWriteTime" );
        multipleReadersMultipleWriters(lockFreeBasedCopyMonitor, writer);
        writer.close();
    	
    	writer = new PrintWriter( singleCopyMonitor.getType() + ".csv" );
        writer.println( "Readers, AvgReadTime, Writers, AvgWriteTime" );
    	multipleReadersMultipleWriters(singleCopyMonitor, writer);
    	writer.close();
    	
    	writer = new PrintWriter( lockBasedCopyMonitor.getType() + ".csv" );
        writer.println( "Readers, AvgReadTime, Writers, AvgWriteTime" );
    	multipleReadersMultipleWriters(lockBasedCopyMonitor, writer);
    	writer.close();
    	
    	writer = new PrintWriter( semBasedCopyMonitor.getType() + ".csv" );
        writer.println( "Readers, AvgReadTime, Writers, AvgWriteTime" );
    	multipleReadersMultipleWriters(semBasedCopyMonitor, writer);
    	writer.close();
    }
}