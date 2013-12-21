
package net.lab0.nebula.data;


/**
 * A synchronized counter
 * 
 * @author 116
 * 
 */
@Deprecated
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
    
    public synchronized void increment(long quantity)
    {
        count += quantity;
    }
    
    public synchronized void decrement()
    {
        --count;
    }
    
    public synchronized void decrement(long quantity)
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
