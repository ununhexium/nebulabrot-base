
package net.lab0.nebula.data;


public class SynchronizedCounter
{
    private long count;
    
    public SynchronizedCounter(long value)
    {
        count = value;
    }
    
    public SynchronizedCounter()
    {
        this(0);
    }
    
    public synchronized void increment()
    {
        ++count;
    }
    
    public synchronized void decrement()
    {
        --count;
    }
    
    public boolean isNonZero()
    {
        return count != 0;
    }
    
    public boolean isPositive()
    {
        return count > 0;
    }

    public long getValue()
    {
        return count;
    }
}
