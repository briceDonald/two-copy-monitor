package test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TestData
{
    AtomicLong timeToWrite;
    AtomicLong timeToRead;
    
    AtomicInteger writeCount;
    AtomicInteger readCount;

    public TestData()
    {
    	timeToRead  = new AtomicLong(0);
    	timeToWrite = new AtomicLong(0);
    	writeCount  = new AtomicInteger(0);
    	readCount   = new AtomicInteger(0);
    }
    
    public void newWrite( long writeTime )
    {
    	timeToWrite.addAndGet(writeTime);
    	writeCount.incrementAndGet();
    }
    
    public void newRead( long readTime )
    {
    	timeToRead.addAndGet(readTime);
    	readCount.incrementAndGet();
    }
    

    private String format(long val )
    {
    	return String.format("% 10d", val);
    }
    
    public String getResults( int readers, int writers, long maxWrTime)
    {
    	long avgReadTime = timeToRead.get()  / readCount.get();
    	long avgWritTime = timeToWrite.get() / writeCount.get();
    	
    	System.out.println( " Rdrs: " + format(readers) + " avgRd: " + format(avgReadTime)  + " RdCt: " + format(readCount.get()) + " Wrts: " + format(writers) +
    						" avgWr: " + format(avgWritTime)  + " WrCt: " + format(writeCount.get()) + " maxWrtime: " + format(maxWrTime) );
    	
    	return 	readers + ", " + avgReadTime + ", " + readCount.get()  + ", " + 
    			writers + ", " + avgWritTime + ", " + writeCount.get() + ", " + maxWrTime;
    }
}
