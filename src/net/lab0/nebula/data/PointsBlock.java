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
    
    /**
     * Creates a points' block and associates it with the given manager for the releasing operation.
     * 
     * @param size
     *            The size this PointsBlock will have.
     * @param manager
     *            The manager of this PointsBlock.
     */
    public PointsBlock(int size, PointsBlockManager manager)
    {
        real = new double[size];
        imag = new double[size];
        iter = new long[size];
        this.size = size;
        this.manager = manager;
    }
    
    /**
     * Resets the defaults values of the arrays.
     */
    public void reset()
    {
        for (int i = 0; i < size; ++i)
        {
            real[i] = 0d;
            imag[i] = 0d;
            iter[i] = 0L;
        }
    }
    
    /**
     * Releases this points block, indicating that it can be reused.
     */
    public void release()
    {
        manager.release(this);
    }
}
