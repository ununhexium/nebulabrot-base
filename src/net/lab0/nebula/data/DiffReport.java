package net.lab0.nebula.data;

/**
 * Stores the results of a diff between 2 RawMandelobrotData objects
 * 
 * @since 1.0
 * @author 116@lab0.net
 *
 */
public class DiffReport
{
    private long total1;
    private long total2;
    private long maxDifference;
    private long totalDifference;
    private long differences;
    
    public DiffReport(long total1, long total2, long maxDifference, long totalDifference, long differences)
    {
        super();
        this.total1 = total1;
        this.total2 = total2;
        this.maxDifference = maxDifference;
        this.totalDifference = totalDifference;
        this.differences = differences;
    }
    
    public long getTotal1()
    {
        return total1;
    }
    
    public long getTotal2()
    {
        return total2;
    }
    
    public long getMaxDifference()
    {
        return maxDifference;
    }
    
    public long getTotalDifference()
    {
        return totalDifference;
    }
    
    public long getDifferences()
    {
        return differences;
    }
    
    @Override
    public String toString()
    {
        return "DiffReport [total1=" + total1 + ", total2=" + total2 + ", maxDifference=" + maxDifference + ", totalDifference=" + totalDifference
        + ", differences=" + differences + "]" + " - integrity loss:" + Math.abs(1.0 - (double) total1 / (double) total2);
    }
    
}
