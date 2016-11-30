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
    
    public String getResults( int readers, int writers, long maxWrTime)
    {
    	float avgReadTime  = (float)timeToRead.get()  / readCount.get();
    	float avgWritTime = (float)timeToWrite.get() / writeCount.get();
    	
//    	System.out.println("Rdrs: " + readers + "  \tavgRd: " + (int)avgReadTime  + "  \tRdCt: " + readCount);
//    	System.out.println("Wrts: " + writers + "  \tavgWr: " + (int)avgWritTime  + "  \tWrCt: " + writeCount + " \tmaxWrtime: " + maxWrTime);
    	return 	readers + ", " + readCount.get()  + ", " + avgReadTime + ", " + 
    			writers + ", " + writeCount.get() + ", " + avgWritTime + ", " + maxWrTime;
    }
}
