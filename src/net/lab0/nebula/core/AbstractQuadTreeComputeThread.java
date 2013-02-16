package net.lab0.nebula.core;

import javax.swing.event.EventListenerList;

import net.lab0.nebula.data.SynchronizedCounter;
import net.lab0.nebula.listener.QuadTreeComputeListener;

/**
 * 
 * Common methods for the quad tree computation.
 * 
 * @since 1.0
 * @author 116@lab0.net
 * 
 */
public abstract class AbstractQuadTreeComputeThread
extends Thread
{
    
    /**
     * Counter used for the {@link CPUQuadTreeComputeThread} id generation.
     */
    private static int            counter;
    
    /**
     * The {@link QuadTreeManager} to work with
     */
    protected QuadTreeManager     quadTreeManager;
    
    /**
     * The counter for the maximum number nodes to compute before automatically stopping this thread.
     */
    protected SynchronizedCounter maxNodesToCompute;
    
    /**
     * The count of nodes computes so far.
     */
    protected SynchronizedCounter computedNodes;
    
    /**
     * The amount of nodes to retrieve per call to {@link QuadTreeManager}.getNextNodeToCompute().
     */
    protected int                 computeBlockSize;
    
    /**
     * Listeners
     */
    private EventListenerList     eventListenerList = new EventListenerList();
    
    /**
     * Builds a computing threads with the following parameters
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
        super();
        this.setName(this.getClass().getSimpleName() + "-" + AbstractQuadTreeComputeThread.getNextQuadTreeComputeThreadId());
        this.quadTreeManager = quadTreeManager;
        this.maxNodesToCompute = maxNodesToCompute;
        this.computedNodes = computedNodes;
        this.computeBlockSize = computeBlockSize;
    }
    
    /**
     * 
     * @param listener
     *            The listener to add to this QuadTreeComputeThread.
     */
    public void addQuadTreeComputeListener(QuadTreeComputeListener listener)
    {
        eventListenerList.add(QuadTreeComputeListener.class, listener);
    }
    
    /**
     * 
     * @param value
     *            Fires an <code>nodesLeftToCompute</code> event to all the registered {@link QuadTreeComputeListener}.
     */
    protected void fireNodesLeftToCompute(long value)
    {
        for (QuadTreeComputeListener listener : eventListenerList.getListeners(QuadTreeComputeListener.class))
        {
            listener.nodesLeftToCompute(value);
        }
    }
    
    /**
     * 
     * @param time
     *            Fires an <code>nodesGroupComputeTime</code> event to all the registered {@link QuadTreeComputeListener}.
     */
    protected void fireNodesGroupComputeTime(long time)
    {
        for (QuadTreeComputeListener listener : eventListenerList.getListeners(QuadTreeComputeListener.class))
        {
            listener.nodesGroupComputeTime(time);
        }
    }
    
    /**
     * 
     * @param name
     *            Fires an <code>threadFinished</code> event to all the registered {@link QuadTreeComputeListener}.
     */
    protected void fireThreadFinished(String name)
    {
        for (QuadTreeComputeListener listener : eventListenerList.getListeners(QuadTreeComputeListener.class))
        {
            listener.threadFinished(name);
        }
    }
    
    protected synchronized static int getNextQuadTreeComputeThreadId()
    {
        return ++counter;
    }
    
    /**
     * The computation in made inside this method. Subclasses willing to implement a computation method have to overload it.
     */
    @Override
    public abstract void run();
}
