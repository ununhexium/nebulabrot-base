
package net.lab0.nebula;


import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.SynchronizedCounter;


public class QuadTreeComputeThread
extends Thread
{
    private static int          idCounter;
    
    private QuadTreeManager     quadTreeManager;
    private SynchronizedCounter maxNodesToCompute;
    private SynchronizedCounter computedNodes;
    
    public QuadTreeComputeThread(QuadTreeManager quadTreeManager, SynchronizedCounter maxNodesToCompute, SynchronizedCounter computedNodes)
    {
        super("QuadTreeComputeThread-" + idCounter++);
        this.quadTreeManager = quadTreeManager;
        this.maxNodesToCompute = maxNodesToCompute;
        this.computedNodes = computedNodes;
    }
    
    @Override
    public void run()
    {
        while (!quadTreeManager.stopRequired() && maxNodesToCompute.isPositive())
        {
            // System.out.println("Try next");
            QuadTreeNode node;
            try
            {
                node = quadTreeManager.getNextNodeToCompute(quadTreeManager.getMaxDepth());
                
                if (node != null)
                {
                    // System.out.println(getName()+" "+node.getPath());
                    maxNodesToCompute.decrement();
                    node.computeStatus(quadTreeManager.getPointsPerSide(), quadTreeManager.getMaxIter(), quadTreeManager.getDiffIterLimit());
                    computedNodes.increment();
                    synchronized (quadTreeManager)
                    {
                        quadTreeManager.notifyAll();
                    }
                }
            }
            catch (NoMoreNodesToCompute e)
            {
                break;
            }
        }
    }
}
