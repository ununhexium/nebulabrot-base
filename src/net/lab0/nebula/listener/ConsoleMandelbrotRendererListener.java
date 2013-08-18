package net.lab0.nebula.listener;

import java.text.DecimalFormat;

import net.lab0.nebula.data.RawMandelbrotData;

/**
 * Basic console implementation of the <code>MandelbrotRendererListener</code>. Prints infos in stdout
 */
public class ConsoleMandelbrotRendererListener
implements MandelbrotRendererListener
{
    private double lastPercent   = -1.0d;
    private double displayStep   = 0.01;                     // 1%
    DecimalFormat  decimalFormat = new DecimalFormat("#0.0");
    
    public ConsoleMandelbrotRendererListener()
    {
        
    }
    
    public ConsoleMandelbrotRendererListener(double displayStep)
    {
        this.displayStep = displayStep;
    }
    
    @Override
    public void rendererProgress(long current, long total)
    {
        // System.out.println("Render at " + current + " of " + total);
        
        if ((double) current / (double) total > lastPercent + displayStep)
        {
            lastPercent = (double) current / (double) total;
            System.out.println("Render at " + decimalFormat.format(lastPercent * 100.0) + "% (" + current + " of " + total + ")");
        }
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
