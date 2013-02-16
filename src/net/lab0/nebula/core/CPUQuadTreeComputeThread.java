package net.lab0.nebula.core;

import java.util.List;

import net.lab0.nebula.data.RootQuadTreeNode;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.data.SynchronizedCounter;
import net.lab0.nebula.exception.NoMoreNodesToCompute;

/**
 * 
 * Thread to compute the {@link StatusQuadTreeNode}s' status of the given {@link QuadTreeManager} using CPU only computation power.
 * 
 * @since 1.0
 * @author 116
 * 
 */
public class CPUQuadTreeComputeThread
extends AbstractQuadTreeComputeThread
{
    /**
     * 
     * @param quadTreeManager
     *            The {@link QuadTreeManager} to use for this computation. It will hold the results in its {@link RootQuadTreeNode}
     * @param maxNodesToCompute
     *            The maximum quantity of node to compute before automatically stopping this thread.
     * @param computedNodes
     *            A reference to a counter that will register the number of nodes computed do far.
     * @param computeBlockSize
     *            The amount of nodes to retrieve after each call to {@link QuadTreeManager}.getNextNodeToCompute().
     */
    // TODO: remove the computeBlockSizeParameter and find automatically an appropriate one or give a fixed one ?
    public CPUQuadTreeComputeThread(QuadTreeManager quadTreeManager, SynchronizedCounter maxNodesToCompute, SynchronizedCounter computedNodes,
    int computeBlockSize)
    {
        super(quadTreeManager, maxNodesToCompute, computedNodes, computeBlockSize);
    }
    
    @Override
    public void run()
    {
        quadTreeManager.fireThreadStarted(this.getId(), this.getName());// the id of the current thread
        // while the quad tree manager doesn't require to stop and there are more nodes to compute
        while (!quadTreeManager.stopRequired() && maxNodesToCompute.isPositive())
        {
            try
            {
                // retrieves the next "computeBlockSize" nodes to compute from the quad tree manager
                List<StatusQuadTreeNode> nodes = quadTreeManager.getNextNodeToCompute(quadTreeManager.getMaxDepth(), this.computeBlockSize);
                
                if (!nodes.isEmpty())
                {
                    computeNodes(nodes);
                }
                else
                // if there is nothing to compute : active wait because i'm lazy to do it better and it is more or Less ok.
                {
                    try
                    {
                        quadTreeManager.fireThreadSleeping(getId());
                        Thread.sleep(1000);
                        quadTreeManager.fireThreadResumed(getId());
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            catch (NoMoreNodesToCompute e)
            {
                fireThreadFinished(this.getName());
                break;
            }
        }
    }
    
    /**
     * Computes the INSIDE/OUTSIDE/BROWSED attributes of a node.
     * 
     * @param nodes 
     */
    private void computeNodes(List<StatusQuadTreeNode> nodes)
    {
        // counts the nodes computed so far in the nodes lists
        int computed = 0;
        
        int pointsPerSide = quadTreeManager.getPointsPerSide();
        int maxIter = quadTreeManager.getMaxIter();
        int diffIterLimit = quadTreeManager.getDiffIterLimit();
        
        long start = System.currentTimeMillis();
        for (StatusQuadTreeNode node : nodes)
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
        
        // fire events
        fireNodesGroupComputeTime((end - start));
        fireNodesLeftToCompute(maxNodesToCompute.getValue());
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
