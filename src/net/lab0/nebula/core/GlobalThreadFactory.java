package net.lab0.nebula.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GlobalThreadFactory
implements ThreadFactory
{
    private static final AtomicInteger id = new AtomicInteger(0);
    private ThreadGroup threadGroup;
    private Lock        lock = new ReentrantLock();
    
    public GlobalThreadFactory(ThreadGroup threadGroup)
    {
        this.threadGroup = threadGroup;
    }
    
    @Override
    public Thread newThread(Runnable r)
    {
        try
        {
            lock.lock();
            Thread thread = new Thread(threadGroup, r, "Global Thread - " + id.getAndIncrement());
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        }
        finally
        {
            lock.unlock();
        }
    }
    
}