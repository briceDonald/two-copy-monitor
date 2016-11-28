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
    
    public void showAverages()
    {
    	float avgRead  = (float)timeToRead.get()  / readCount.get();
    	float avgWrite = (float)timeToWrite.get() / writeCount.get();
    	
    	System.out.println("\n");
    	System.out.println("Avg read time: " + avgRead  + "\tReadCount: " + readCount);
    	System.out.println("Avg writ time: " + avgWrite + "\tWritCount: " + writeCount);
    }
}
