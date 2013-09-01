package net.lab0.nebula.data;

import net.lab0.nebula.color.ColorationModel;
import net.lab0.nebula.color.GrayScaleColorModel;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import net.lab0.tools.geom.RectangleInterface;

public class RenderingParameters
{
    private int                xResolution;
    private int                yResolution;
    private long               pointsCount;
    private RectangleInterface viewport;
    private int                minimumIteration;
    private int                maximumIteration;
    private ColorationModel    colorationModel;
    
    public RenderingParameters()
    {
        this(1024, 1024, 1L << 20, 16, 1024, new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0)),
        new GrayScaleColorModel());
    }
    
    public RenderingParameters(int xResolution, int yResolution, long pointsCount, int minimumIteration,
    int maximumIteration, RectangleInterface viewport, ColorationModel colorationModel)
    {
        super();
        this.xResolution = xResolution;
        this.yResolution = yResolution;
        this.pointsCount = pointsCount;
        this.viewport = viewport;
        this.minimumIteration = minimumIteration;
        this.maximumIteration = maximumIteration;
        this.colorationModel = colorationModel;
    }
    
    public int getxResolution()
    {
        return xResolution;
    }
    
    public int getyResolution()
    {
        return yResolution;
    }
    
    public long getPointsCount()
    {
        return pointsCount;
    }
    
    public RectangleInterface getViewport()
    {
        return viewport;
    }
    
    public int getMinimumIteration()
    {
        return minimumIteration;
    }
    
    public int getMaximumIteration()
    {
        return maximumIteration;
    }
    
    public void setxResolution(int xResolution)
    {
        this.xResolution = xResolution;
    }
    
    public void setyResolution(int yResolution)
    {
        this.yResolution = yResolution;
    }
    
    public void setPointsCount(long pointsCount)
    {
        this.pointsCount = pointsCount;
    }
    
    public void setViewport(RectangleInterface viewport)
    {
        this.viewport = viewport;
    }
    
    public void setMinimumIteration(int minimumIteration)
    {
        this.minimumIteration = minimumIteration;
    }
    
    public void setMaximumIteration(int maximumIteration)
    {
        this.maximumIteration = maximumIteration;
    }
    
    public ColorationModel getColorationModel()
    {
        return colorationModel;
    }
    
    public void setColorationModel(ColorationModel colorationModel)
    {
        this.colorationModel = colorationModel;
    }
    
}
