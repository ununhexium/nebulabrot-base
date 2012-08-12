
package net.lab0.nebula.listener;


import java.util.EventListener;

import net.lab0.nebula.data.RawMandelbrotData;


public interface MandelbrotRendererListener
extends EventListener
{
    public void progress(long current, long total);
    
    public void finished(RawMandelbrotData data);
}
