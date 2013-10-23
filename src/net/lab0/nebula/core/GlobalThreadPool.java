package net.lab0.nebula.core;

import net.lab0.tools.exec.PriorityExecutor;

/**
 * A global thread pool for all the application.
 * 
 * @author 116@lab0.net
 * 
 */
public class GlobalThreadPool
extends PriorityExecutor
{
    private static GlobalThreadPool globalThreadPool;
    
    private GlobalThreadPool(int coreThreadsCount)
    {
        super(coreThreadsCount);
        this.setThreadFactory(new GlobalThreadFactory(new ThreadGroup("GTP")));
    }
    
    /**
     * Instantiates a GlobalThreadPool with <code>Runtime.getRuntime().availableProcessors()</code> core threads.
     * 
     * @return the singleton
     */
    public static synchronized GlobalThreadPool getInstance()
    {
        if (globalThreadPool == null)
        {
            globalThreadPool = new GlobalThreadPool(Runtime.getRuntime().availableProcessors());
        }
        return globalThreadPool;
    }
}
