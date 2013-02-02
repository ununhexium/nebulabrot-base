package net.lab0.nebula.listener;

import java.util.EventListener;

import net.lab0.nebula.data.RawMandelbrotData;

public interface MandelbrotRendererListener
extends EventListener
{
    public void rendererProgress(long current, long total);
    
    public void rendererFinished(RawMandelbrotData raw);
    
    public void rendererStopped(RawMandelbrotData raw);
}
