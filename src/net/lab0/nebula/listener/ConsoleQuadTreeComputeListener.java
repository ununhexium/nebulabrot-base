package net.lab0.nebula.listener;

/**
 * A console output implementation of the {@link QuadTreeComputeListener}.
 * 
 * @author 116@lab0.net
 * 
 */
public class ConsoleQuadTreeComputeListener
implements QuadTreeComputeListener
{
    
    @Override
    public void nodesGroupComputeTime(long time)
    {
        System.out.println("Nodes group total time = " + time + ".");
    }
    
    @Override
    public void nodesLeftToCompute(long count)
    {
        System.out.println("Still " + count + " nodes to compute");
    }
    
    @Override
    public void threadFinished(String name)
    {
        System.out.println("No more nodes to compute for thread " + name);
    }
    
}
