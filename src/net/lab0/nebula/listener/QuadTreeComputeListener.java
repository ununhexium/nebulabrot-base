package net.lab0.nebula.listener;

import java.util.EventListener;

public interface QuadTreeComputeListener
extends EventListener
{
    public void nodesGroupComputeTime(long time);
    
    public void nodesLeftToCompute(long count);
    
    public void threadFinished(String name);
}
