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
    
    /**
     * Checks that the lengths of the arrays and the size are the same.
     * 
     * @return <code>true</code> if the length is the same. <code>false</code> otherwise.
     */
    public boolean isConsistent()
    {
        return size == real.length && size == imag.length && size == iter.length;
    }
    
    /**
     * Copies <code>real</code> into <code>this.real</code>
     * 
     * @param real
     *            the array to copy
     * @throws IllegalArgumentException
     *             If the 2 arrays don't have the same length.
     */
    public void copyReal(double[] real)
    {
        if (real.length != this.real.length)
        {
            throw new IllegalArgumentException("The two arrays must have the same length: real=" + real.length
            + ", this.real=" + this.real.length);
        }
        
        for (int i = 0; i < real.length; ++i)
        {
            this.real[i] = real[i];
        }
    }
    
    /**
     * Copies <code>imag</code> into <code>this.imag</code>
     * 
     * @param imag
     *            the array to copy
     * @throws IllegalArgumentException
     *             If the 2 arrays don't have the same length.
     */
    public void copyImag(double[] imag)
    {
        if (imag.length != this.imag.length)
        {
            throw new IllegalArgumentException("The two arrays must have the same length: imag=" + imag.length
            + ", this.imag=" + this.imag.length);
        }
        
        for (int i = 0; i < imag.length; ++i)
        {
            this.imag[i] = imag[i];
        }
    }
    
    /**
     * Copies <code>iter</code> into <code>this.iter</code>
     * 
     * @param iter
     *            the array to copy
     * @throws IllegalArgumentException
     *             If the 2 arrays don't have the same length.
     */
    public void copyIter(long[] iter)
    {
        if (iter.length != this.iter.length)
        {
            throw new IllegalArgumentException("The two arrays must have the same length: iter=" + iter.length
            + ", this.iter=" + this.iter.length);
        }
        
        for (int i = 0; i < iter.length; ++i)
        {
            this.iter[i] = iter[i];
        }
    }
}
