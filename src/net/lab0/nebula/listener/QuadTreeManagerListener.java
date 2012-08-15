package net.lab0.nebula.listener;

import java.util.EventListener;

public interface QuadTreeManagerListener
extends EventListener
{
    public void computeProgress(int current, int total);
    
    public void computationFinished(boolean remaining);
}
