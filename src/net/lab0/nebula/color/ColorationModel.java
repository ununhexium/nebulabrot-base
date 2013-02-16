package net.lab0.nebula.color;

/**
 * A class must implement this interface to be used as a coloration model.
 * 
 * @since 1.0
 * @author 116
 * 
 */
public interface ColorationModel
{
    /**
     * Computes the red, green and blue values for a given point. You may give more than 1 value for a point. i.e. give one value for the red, one value for the
     * green and one value for the blue or more, depending on your creativity, to compute the resulting color. The values are a number of iterations, min and
     * max.
     * 
     * @param vector
     *            The resulting RGB values of the computation. The vector is a <code>float[3]</code>.
     * @param values
     *            The values associated to a pixel of a given rendering set (iterations, min, max).
     */
    public void computeColorForPoint(float[] vector, PointValues... values);
    
    /**
     * 
     * @return The number of channels this coloration model requires to work. Should be at least 1.
     */
    public int getChannelsCount();
}
