
package net.lab0.nebula.listener;


public class ConsoleQuadTreeManagerListener
implements QuadTreeManagerListener
{
    @Override
    public void threadStarted(long threadId)
    {
        System.out.println("Thread " + threadId + " started");
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
        System.out.println("Loadin file " + current + " out of " + total);
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
    
}
