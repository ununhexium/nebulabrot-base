
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
    
    public synchronized void increment(int quantity)
    {
        count += quantity;
    }
    
    public synchronized void decrement()
    {
        --count;
    }
    
    public synchronized void decrement(int quantity)
    {
        count -= quantity;
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
