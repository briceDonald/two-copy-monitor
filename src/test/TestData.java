package test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TestData
{
    AtomicLong timeToWrite;
    AtomicLong timeToRead;
    
    AtomicInteger writeCount;
    AtomicInteger readCount;
    
    float avgRead;
	float avgWrite;

    public TestData()
    {
    	timeToRead  = new AtomicLong(0);
    	timeToWrite = new AtomicLong(0);
    	writeCount  = new AtomicInteger(0);
    	readCount   = new AtomicInteger(0);
    }
    
    public void newWrite( long writeTime )
    {
//    	if( writeTime > (3*avgWrite) )
//    		return;
    	
    	timeToWrite.addAndGet(writeTime);
    	writeCount.incrementAndGet();
    	
//    	System.out.println("----: " + writeTime + "\t Total: " + timeToWrite.get());
    }
    
    public void newRead( long readTime )
    {
//    	if( readTime > (3*avgRead) )
//    		return;
    	timeToRead.addAndGet(readTime);
    	readCount.incrementAndGet();
    }
    
    public String getResults()
    {
    	float avgRead  = timeToRead.get()  / readCount.get();
    	float avgWrite = timeToWrite.get() / writeCount.get();
    	
//    	System.out.println("\n");
    	System.out.println("Avg read time: " + avgRead  + "\tReadCount: " + readCount + "\treadTime: " + timeToRead.get());
    	System.out.println("Avg writ time: " + avgWrite + "\tWritCount: " + writeCount  + "\tWriteTime: " + timeToWrite.get());
    	return readCount.get() + ", " + avgRead + ", " + writeCount.get() + ", " + avgWrite;
    }
}
