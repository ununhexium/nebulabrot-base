
package net.lab0.nebula.core;


import java.util.ArrayList;
import java.util.List;

import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.SynchronizedCounter;
import net.lab0.nebula.exception.NoMoreNodesToCompute;


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
            // System.out.println(Thread.currentThread().getName() + " Try next");
            try
            {
                List<QuadTreeNode> nodes = quadTreeManager.getNextNodeToCompute(quadTreeManager.getMaxDepth());
                
                if (!nodes.isEmpty())
                {
                    List<Long> times = new ArrayList<>(nodes.size());
                    int computed = 0;
                    System.out.println("Retrieved " + nodes.size() + " nodes");
                    
                    for (QuadTreeNode node : nodes)
                    {
                        if (node != null)
                        {
                            // System.out.println(Thread.currentThread().getName() + " computing");
                            // System.out.println(getName()+" "+node.getPath());
                            long start = System.currentTimeMillis();
                            node.computeStatus(quadTreeManager.getPointsPerSide(), quadTreeManager.getMaxIter(), quadTreeManager.getDiffIterLimit());
                            computed++;
                            long end = System.currentTimeMillis();
                            times.add(end - start);
                            // System.out.println("" + (end - start));
                        }
                    }
                    
                    maxNodesToCompute.decrement(computed);
                    computedNodes.increment(computed);
                    
                    long total = 0;
                    for (Long l : times)
                    {
                        total += l;
                    }
                    System.out.println("Mean time = " + total / 1024);
                }
                else
                {
                    try
                    {
                        // System.out.println(Thread.currentThread().getName() + " sleeping");
                        Thread.sleep(1000);
                        // System.out.println(Thread.currentThread().getName() + " resumed");
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
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
