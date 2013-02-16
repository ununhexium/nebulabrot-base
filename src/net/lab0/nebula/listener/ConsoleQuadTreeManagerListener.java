package net.lab0.nebula.listener;

import net.lab0.tools.HumanReadable;

/**
 * Basic console implementation of the <code>QuadTreeManagerListener</code> interface
 */
public class ConsoleQuadTreeManagerListener
implements QuadTreeManagerListener
{
    @Override
    public void threadStarted(long threadId, String name)
    {
        System.out.println("Thread " + threadId + " (" + name + ") started");
    }
    
    @Override
    public void threadSleeping(long threadId)
    {
        System.out.println("Thread " + threadId + " is sleeping");
    }
    
    @Override
    public void threadResumed(long threadId)
    {
        System.out.println("Thread " + threadId + " resumed");
    }
    
    @Override
    public void loadingFile(int current, int total)
    {
        System.out.println("Loading file " + current + " out of " + total);
    }
    
    @Override
    public void computeProgress(int current, int total)
    {
        System.out.println("Progress : " + current + " / " + total);
    }
    
    @Override
    public void computationFinished(boolean remaining)
    {
        System.out.println("Computation finished. Remaining=" + remaining);
    }
    
    @Override
    public void loadingOfCurrentFileProgress(long current, long total)
    {
        System.out.println("Loading " + HumanReadable.humanReadableByteCount(current, true) + " out of " + HumanReadable.humanReadableByteCount(total, true));
    }
    
}
