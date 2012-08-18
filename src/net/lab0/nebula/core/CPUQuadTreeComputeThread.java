
package net.lab0.nebula.core;


import java.util.List;

import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.SynchronizedCounter;
import net.lab0.nebula.exception.NoMoreNodesToCompute;


/**
 * 
 * Thread to compute the {@link QuadTreeNode}s' status of the given {@link QuadTreeManager}
 * 
 * @author 116
 * 
 */
public class CPUQuadTreeComputeThread
extends AbstractQuadTreeComputeThread
{
    
    public CPUQuadTreeComputeThread(QuadTreeManager quadTreeManager, SynchronizedCounter maxNodesToCompute, SynchronizedCounter computedNodes,
    int computeBlockSize)
    {
        super(quadTreeManager, maxNodesToCompute, computedNodes, computeBlockSize);
    }
    
    @Override
    public void run()
    {
        // while the quad tree manager doesn't require to stop and there are more nodes to compute
        while (!quadTreeManager.stopRequired() && maxNodesToCompute.isPositive())
        {
            try
            {
                // retirves the next 16 nodes to compute from the quad tree manager
                List<QuadTreeNode> nodes = quadTreeManager.getNextNodeToCompute(quadTreeManager.getMaxDepth(), computeBlockSize);
                
                if (!nodes.isEmpty())
                {
                    int computed = 0;
                    // System.out.println("Retrieved " + nodes.size() + " nodes. Still " +maxNodesToCompute.getValue()+" nodes to compute");
                    // TODO : change sysout to event
                    
                    int pointsPerSide = quadTreeManager.getPointsPerSide();
                    int maxIter = quadTreeManager.getMaxIter();
                    int diffIterLimit = quadTreeManager.getDiffIterLimit();
                    
                    long start = System.currentTimeMillis();
                    for (QuadTreeNode node : nodes)
                    {
                        node.computeStatus(pointsPerSide, maxIter, diffIterLimit);
                        computed++;
                        
                        // checks whether this thread need to stop and leave the main while
                        if (quadTreeManager.stopRequired())
                        {
                            break;
                        }
                    }
                    long end = System.currentTimeMillis();
                    
                    quadTreeManager.computedNodes(computed);
                    maxNodesToCompute.decrement(computed);
                    computedNodes.increment(computed);
                    
                    // long total = 0;
                    // for (Long l : times)
                    // {
                    // total += l;
                    // }
                    System.out.println("Total time = " + (end - start) + ". Still " + maxNodesToCompute.getValue() + " nodes to compute");
                }
                else
                // if there is nothing to compute : active wait
                {
                    try
                    {
                        // System.out.println(Thread.currentThread().getName() + " sleeping"); TODO : change sysout to event
                        Thread.sleep(1000);
                        // System.out.println(Thread.currentThread().getName() + " resumed"); TODO : change sysout to event
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            catch (NoMoreNodesToCompute e)
            {
                System.out.println("Mo more nodes to compute");
                break;
            }
        }
    }
    
    public int getComputeBlockSize()
    {
        return computeBlockSize;
    }
    
    public void setComputeBlockSize(int computeBlockSize)
    {
        this.computeBlockSize = computeBlockSize;
    }
    
}
