package net.lab0.nebula.data;

import net.lab0.nebula.mgr.PointsBlockManager;

/**
 * This is a data block. Once initialised. Set to be public for access performance reasons.
 * 
 * @author 116@lab0.net
 * 
 */
public class PointsBlock
{
    public final double        real[];
    public final double        imag[];
    public final long          iter[];
    public final int           size;
    private PointsBlockManager manager;
    
    public PointsBlock(int size, PointsBlockManager manager)
    {
        real = new double[size];
        imag = new double[size];
        iter = new long[size];
        this.size = size;
        this.manager = manager;
    }
    
    public void reset()
    {
        for (int i = 0; i < size; ++i)
        {
            real[i] = 0d;
            imag[i] = 0d;
            iter[i] = 0L;
        }
    }
    
    public void free()
    {
        manager.free(this);
    }
}
