package net.lab0.nebula.listener;

import net.lab0.nebula.data.RawMandelbrotData;

/**
 * Basic console implementation of the <code>MandelbrotRendererListener</code>. Prints infos in stdout
 */
public class ConsoleMandelbrotRendererListener
implements MandelbrotRendererListener
{
    
    @Override
    public void rendererProgress(long current, long total)
    {
        System.out.println("Render at " + current + " of " + total);
    }
    
    @Override
    public void rendererFinished(RawMandelbrotData raw)
    {
        System.out.println("Rendering finished for " + raw);
    }
    
    @Override
    public void rendererStopped(RawMandelbrotData raw)
    {
        System.out.println("Rendering stopped for " + raw);
    }
    
}
