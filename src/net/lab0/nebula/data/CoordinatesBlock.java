package net.lab0.nebula.data;

public class CoordinatesBlock
{
    public double minX, maxX, minY, maxY, stepX, stepY;
    
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
