package net.lab0.nebula.color;

/**
 * A class must implement this interface to be used as a coloration model.
 * 
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
     *            the resulting RGB values of the computation
     * @param values
     *            the values associated to a pixel of a given rendering set (iterations, min, max)
     */
    public void computeColorForPoint(float[] vector, PointValues... values); // float[3]
    
    /**
     * 
     * @return the number of channels this coloration model needs to work
     */
    public int getChannelsCount();
}
