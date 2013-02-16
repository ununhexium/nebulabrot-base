package net.lab0.nebula.color;

/**
 * 
 * This class wraps value for the coloration models. A coloration models may use several of these PointValue for a single pixel to compute mode complex colors.
 * 
 * @since 1.0
 * @author 116@lab0.net
 * 
 */
public class PointValues
{
    /**
     * The minimum value in the iterations for the current coloration.
     */
    public long minIter;
    
    /**
     * The maximum value in the iterations for the current coloration.
     */
    public long maxIter;
    
    /**
     * The actual value to compute the color with.
     */
    public long value;
}
