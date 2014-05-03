package net.lab0.nebula.exe;

import java.util.HashMap;
import java.util.Map;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.enums.Status;
import net.lab0.tools.exec.CascadingJob;

/**
 * Aggregates statistics about {@link MandelbrotQuadTreeNode}s
 * 
 * @author 116@lab0.net
 * 
 */
public class MandelbrotQTNStats
extends CascadingJob<MandelbrotQuadTreeNode[], Void>
{
    /**
     * The aggregated data
     * 
     * @author 116@lab0.net
     * 
     */
    public static class Aggregate
    {
        private Map<Status, Long> counts = new HashMap<Status, Long>();
        
        public Aggregate()
        {
            for (Status s : Status.values())
            {
                counts.put(s, 0L);
            }
        }
        
        public synchronized void add(Status status, long q)
        {
            counts.put(status, counts.get(status) + q);
        }
        
        public Map<Status, Long> getCounts()
        {
            return counts;
        }
        
        public long getTotal()
        {
            long total = 0;
            for (Status s : Status.values())
            {
                total += counts.get(s);
            }
            return total;
        }
    }
    
    private MandelbrotQuadTreeNode[] input;
    private Aggregate                aggregate;
    
    public MandelbrotQTNStats(CascadingJob<?, MandelbrotQuadTreeNode[]> parent, MandelbrotQuadTreeNode[] input,
    Aggregate aggregate)
    {
        super(parent, null);
        this.input = input;
        this.aggregate = aggregate;
    }
    
    @Override
    public void executeTask()
    throws Exception
    {
        long in = 0;
        long out = 0;
        long browsed = 0;
        long v = 0;
        
        for (MandelbrotQuadTreeNode node : input)
        {
            switch (node.status)
            {
                case BROWSED:
                    browsed++;
                    break;
                
                case INSIDE:
                    in++;
                    break;
                
                case OUTSIDE:
                    out++;
                    break;
                
                case VOID:
                    v++;
                    break;
            
            }
        }
        
        aggregate.add(Status.BROWSED, browsed);
        aggregate.add(Status.INSIDE, in);
        aggregate.add(Status.OUTSIDE, out);
        aggregate.add(Status.VOID, v);
        
    }
    
}
