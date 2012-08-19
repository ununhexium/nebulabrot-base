
package net.lab0.nebula.listener;


import java.util.EventListener;


public interface QuadTreeManagerListener
extends EventListener
{
    public void computeProgress(int current, int total);
    
    public void computationFinished(boolean remaining);
    
    public void threadSleeping(long threadId);
    
    public void threadResumed(long threadId);
    
    public void threadStarted(long threadId);
    
    public void loadingFile(int current, int total);
}
