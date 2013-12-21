package net.lab0.nebula.data;

/**
 * A block of coordinates + the X and Y step that should be used for browsing them.
 * 
 * @author 116
 * 
 */
public class CoordinatesBlock
{
    public double minX, maxX, minY, maxY, stepX, stepY;
    
    /**
     * Create a block with the given coordinates.
     * 
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     * @param stepX
     * @param stepY
     */
    public CoordinatesBlock(double minX, double maxX, double minY, double maxY, double stepX, double stepY)
    {
        super();
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.stepX = stepX;
        this.stepY = stepY;
    }
    
}
