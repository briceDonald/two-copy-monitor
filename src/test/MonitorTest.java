package test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


import org.junit.*;
import static org.junit.Assert.*;



import Monitor.LockBasedTwoCopyMonitor;
import Monitor.LockFreeBasedTwoCopyMonitor;
import Monitor.MonitorObj;
import Monitor.SemBasedTwoCopyMonitor;
import Monitor.SingleCopyMonitor;

//package test;


/**
 * Created by abed on 11/22/16.
 */
public class MonitorTest
{
	long PERIOD_NANOS = 1000000000;
	MonitorObj<Integer> singleCopyMonitor;
	MonitorObj<Integer> lockBasedCopyMonitor;
	MonitorObj<Integer> semBasedCopyMonitor;
	MonitorObj<Integer> lockFreeBasedCopyMonitor;

    @Before public void initialize()
    {
    	singleCopyMonitor = new SingleCopyMonitor<Integer>(5);
    	lockBasedCopyMonitor = new LockBasedTwoCopyMonitor<Integer>(5);
    	semBasedCopyMonitor = new SemBasedTwoCopyMonitor<Integer>(5);
    	lockFreeBasedCopyMonitor = new LockFreeBasedTwoCopyMonitor<Integer>(5);
    }

    
    public void multipleReadersMultipleWriters( int iterations, MonitorObj<Integer> monitor, PrintWriter writer) throws InterruptedException
    {
    	System.out.println("\n" + monitor.getType());
    	for(int i = 0; i < iterations; i++)
    	{
    		int v = i + 1;
    		writer.println( runTest(monitor, v, v) );
    	}
    }


    private String runTest( final MonitorObj<Integer> monitor, 
    					    final int numWriterThreads, final int numReaderThreads)
    {
    	final TestData testData = new TestData();
        final Future<Boolean>[] writerFutures = new Future[numWriterThreads];
        final Future<Boolean>[] readerFutures = new Future[numReaderThreads];
        final AtomicLong maxWriteTime = new AtomicLong(0);
        
        Thread createWriterFutures = new Thread(new Runnable()
        {
            public void run()
            {
            	final long startTime = System.nanoTime();
            	ExecutorService writersPool = Executors.newFixedThreadPool(numWriterThreads);

                for (int i = 0; i < numWriterThreads; i++)
                {
                    writerFutures[i] = (writersPool.submit(new Callable<Boolean>()
                    {
                        public Boolean call()
                        {
                        	long delta;
                        	long endTime;
                            long curTime = System.nanoTime();
                            
                            while(curTime - startTime < PERIOD_NANOS)
                            {
                            	curTime = System.nanoTime();
                          	  	monitor.set(1);
                          	  	endTime = System.nanoTime();
                          	  	delta = endTime - curTime;

                          	  	if(maxWriteTime.get() < delta)
                          	  		maxWriteTime.set(delta);
                          	  	
                          	  	// do no record any write after the PERIOD+NANO elapsed
                        	  	if( endTime - startTime > PERIOD_NANOS )
                        	  	{
                        	  		break;
                        	  	}
                          	  	testData.newWrite( delta );
                            }
                            
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
            	final long startTime = System.nanoTime();
            	ExecutorService readersPool = Executors.newFixedThreadPool(numReaderThreads);
            	
                for (int i = 0; i < numReaderThreads; i++)
                {
                    readerFutures[i] = readersPool.submit(new Callable<Boolean>()
                    {
                        public Boolean call()
                        {                       
                        	long delta;
                        	long endTime;
                        	long maxReadTime = 0;
                        	long curTime = System.nanoTime();
                          	                          	
                          	while(curTime - startTime < PERIOD_NANOS)
                          	{
                        	  	curTime = System.nanoTime();
                        	  	monitor.get();
                        	  	endTime = System.nanoTime();
                        	  	delta = endTime - curTime;

                        	  	if( maxReadTime < delta)
                        		  	maxReadTime = delta;
                        	  	
                        	  	// do no record any write after the PERIOD+NANO elapsed
                        	  	if( endTime - startTime > PERIOD_NANOS )
                        	  	{
                        	  		break;
                        	  	}

                        	  	testData.newRead( delta );
                          	}
                          	
                          	return true;
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

        createReaderFutures.start();
        createWriterFutures.start();

        try
        {
        	// Wait for all futures to complete
            createWriterFutures.join();
            createReaderFutures.join();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        return testData.getResults(numReaderThreads, numWriterThreads, maxWriteTime.get());
    }    


    @Test
    public void testMonitors() throws InterruptedException, FileNotFoundException
    {
    	int iterations = 10;
    	PrintWriter writer;
    	String labels = "NumReaders, AvgReadTime, NumReads, NumWriters, AvgWriteTime, NumWrites, MaxWriteTime";
        writer = new PrintWriter( lockFreeBasedCopyMonitor.getType() + ".csv" );
        writer.println( labels );
        multipleReadersMultipleWriters(iterations, lockFreeBasedCopyMonitor, writer);
        writer.close();
    	
    	writer = new PrintWriter( singleCopyMonitor.getType() + ".csv" );
        writer.println( labels );
    	multipleReadersMultipleWriters(iterations, singleCopyMonitor, writer);
    	writer.close();
    	
    	writer = new PrintWriter( lockBasedCopyMonitor.getType() + ".csv" );
        writer.println( labels );
    	multipleReadersMultipleWriters(iterations, lockBasedCopyMonitor, writer);
    	writer.close();
    	
    	writer = new PrintWriter( semBasedCopyMonitor.getType() + ".csv" );
        writer.println( labels );
    	multipleReadersMultipleWriters(iterations, semBasedCopyMonitor, writer);
    	writer.close();
    }
}