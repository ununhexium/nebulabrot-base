package net.lab0.nebula.data;


/**
 * This is a data block. Set to be public for access performance reasons.
 * 
 * @author 116@lab0.net
 * 
 */
public class PointsBlock
{
    public final double real[];
    public final double imag[];
    public final long   iter[];
    public final int    size;
    
    /**
     * Creates a points' block and associates it with the given manager for the releasing operation.
     * 
     * @param size
     *            The size this PointsBlock will have.
     */
    public PointsBlock(int size)
    {
        real = new double[size];
        imag = new double[size];
        iter = new long[size];
        this.size = size;
    }
    
    /**
     * Creates a points' block and associates it with the given manager for the releasing operation.
     * 
     * @param size
     *            The size this PointsBlock will have.
     * @param real
     *            The reals array
     * @param imag
     *            The imags array.
     * 
     * @throws IllegalArgumentException
     *             If the size and lengths are not the same.
     */
    public PointsBlock(int size, double[] real, double[] imag)
    {
        if (size != real.length || size != imag.length)
        {
            throw new IllegalArgumentException("The size and lengths must be the same");
        }
        this.real = real;
        this.imag = imag;
        iter = new long[size];
        this.size = size;
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
}
