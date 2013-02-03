package net.lab0.nebula.core;

import javax.swing.event.EventListenerList;

import net.lab0.nebula.data.SynchronizedCounter;
import net.lab0.nebula.listener.QuadTreeComputeListener;

public abstract class AbstractQuadTreeComputeThread
extends Thread
{
    
    /**
     * counter used for the {@link CPUQuadTreeComputeThread} id generation
     */
    private static int            counter;
    
    /**
     * The {@link QuadTreeManager} to work with
     */
    protected QuadTreeManager     quadTreeManager;
    
    /**
     * the counter for the maximum number nodes to computes before stopping this thread
     */
    protected SynchronizedCounter maxNodesToCompute;
    
    /**
     * the count of nodes computes so far
     */
    protected SynchronizedCounter computedNodes;
    
    /**
     * the amount of nodes to retrieve per call to QuadTreeManager.getNextNodeToCompute
     */
    protected int                 computeBlockSize;
    
    /**
     * Listeners
     */
    private EventListenerList     eventListenerList = new EventListenerList();
    
    /**
     * Builds a computing threqds with the folowwing parameters
     * 
     * @param quadTreeManager
     *            the quad tree containing the nodes to be computed
     * @param maxNodesToCompute
     *            the maximum number of nodes to computes
     * @param computedNodes
     *            the number of computed nodes
     * @param computeBlockSize
     *            the size of a computation block
     */
    public AbstractQuadTreeComputeThread(QuadTreeManager quadTreeManager, SynchronizedCounter maxNodesToCompute, SynchronizedCounter computedNodes,
    int computeBlockSize)
    {
        super("CPUQuadTreeComputeThread-" + ++counter);
        this.quadTreeManager = quadTreeManager;
        this.maxNodesToCompute = maxNodesToCompute;
        this.computedNodes = computedNodes;
        this.computeBlockSize = computeBlockSize;
    }
    
    public void addQuadTreeComputeListener(QuadTreeComputeListener listener)
    {
        eventListenerList.add(QuadTreeComputeListener.class, listener);
    }
    
    protected void fireNodesLeftToCompute(long value)
    {
        for (QuadTreeComputeListener listener : eventListenerList.getListeners(QuadTreeComputeListener.class))
        {
            listener.nodesLeftToCompute(value);
        }
    }
    
    protected void fireNodesGroupComputeTime(long time)
    {
        for (QuadTreeComputeListener listener : eventListenerList.getListeners(QuadTreeComputeListener.class))
        {
            listener.nodesGroupComputeTime(time);
        }
    }
    
    protected void fireThreadFinished(String name)
    {
        for (QuadTreeComputeListener listener : eventListenerList.getListeners(QuadTreeComputeListener.class))
        {
            listener.threadFinished(name);
        }
    }
    
    @Override
    public abstract void run();
}
