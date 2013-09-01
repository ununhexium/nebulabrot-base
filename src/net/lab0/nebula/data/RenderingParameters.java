package net.lab0.nebula.data;

import net.lab0.nebula.color.ColorationModel;
import net.lab0.nebula.color.GrayScaleColorModel;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import net.lab0.tools.geom.RectangleInterface;

/**
 * Groups the rendering parameters for the nebula fractal.
 * 
 * @author 116@lab0.net
 * 
 */
public class RenderingParameters
{
    private int                xResolution;
    private int                yResolution;
    private long               pointsCount;
    private RectangleInterface viewport;
    private int                minimumIteration;
    private int                maximumIteration;
    private ColorationModel    colorationModel;
    
    /**
     * This is equivalent to
     * 
     * <pre>
     * this(1024, // x resolution
     * 1024, // y resolution
     * 1L &lt;&lt; 20, // points count
     * 16, // min iter
     * 1024, // max iter
     * new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0)), new GrayScaleColorModel());
     * </pre>
     */
    public RenderingParameters()
    {
        this(1024, 1024, 1L << 20, 16, 1024, new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0)),
        new GrayScaleColorModel());
    }
    
    /**
     * Build a new set of rendering parameters.
     * 
     * @param xResolution
     *            The raw data X resolution. must be > 0
     * @param yResolution
     *            The raw data Y resolution. must be > 0
     * @param pointsCount
     *            The amount of points to compute. must be > 0, preferably a power of 4
     * @param minimumIteration
     *            The minimum iteration a point has to have to be computed. must be > 0 and <= maximumIteration
     * @param maximumIteration
     *            The maximum number of iterations to do before assuming that the point is inside the Mandelbrot set.
     *            must be > 0 and >= minimumIteration
     * @param viewport
     *            The area to render
     * @param colorationModel
     *            The coloration
     */
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
    
    /**
     * 
     * @param xResolution
     *            the X resolution. Must be > 0.
     * 
     * @throws IllegalArgumentException
     *             If xResolution <= 0.
     */
    public void setxResolution(int xResolution)
    throws IllegalArgumentException
    {
        if (xResolution <= 0)
        {
            throw new IllegalArgumentException("xResolution can't be <= 0.");
        }
        this.xResolution = xResolution;
    }
    
    /**
     * 
     * @param yResolution
     *            the Y resolution. Must be > 0.
     * 
     * @throws IllegalArgumentException
     *             If yResolution <= 0.
     */
    public void setyResolution(int yResolution)
    {
        if (yResolution <= 0)
        {
            throw new IllegalArgumentException("yResolution can't be <= 0.");
        }
        this.yResolution = yResolution;
    }
    
    /**
     * 
     * @param pointsCount
     *            The amount of points to compute. Must be > 0.
     * 
     * @throws IllegalArgumentException
     *             If pointsCount <= 0.
     */
    public void setPointsCount(long pointsCount)
    {
        if (pointsCount <= 0)
        {
            throw new IllegalArgumentException("pointsCount can't be <= 0.");
        }
        this.pointsCount = pointsCount;
    }
    
    /**
     * Sets the viewport. Can't be null.
     * 
     * @param viewport
     * 
     * @throws NullPointerException
     *             if the viewport is null.
     */
    public void setViewport(RectangleInterface viewport)
    {
        if (viewport == null)
        {
            throw new NullPointerException("The viewport can't be null.");
        }
        this.viewport = viewport;
    }
    
    /**
     * @param minimumIteration
     *            The minimum iterations count. Must be >= 0.
     * 
     * @throws IllegalArgumentException
     *             If minimumIteration < 0. Or if minimumIteration > maximumIteration.
     */
    public void setMinimumIteration(int minimumIteration)
    {
        if (minimumIteration < 0)
        {
            throw new IllegalArgumentException("minimumIteration can't be < 0.");
        }
        if (minimumIteration > maximumIteration)
        {
            throw new IllegalArgumentException("minimumIteration can't be > maximumIteration");
        }
        this.minimumIteration = minimumIteration;
    }
    
    /**
     * @param maximumIteration
     *            The maximum iterations count. Must be >= 0.
     * 
     * @throws IllegalArgumentException
     *             If maximumIteration < 0. Or if maximumIteration < minimumIteration.
     */
    public void setMaximumIteration(int maximumIteration)
    {
        if (maximumIteration < 0)
        {
            throw new IllegalArgumentException("maximumIteration can't be < 0.");
        }
        if (minimumIteration > maximumIteration)
        {
            throw new IllegalArgumentException("maximumIteration can't be < minimumIteration");
        }
        this.maximumIteration = maximumIteration;
    }
    
    public ColorationModel getColorationModel()
    {
        return colorationModel;
    }
    
    /**
     * Sets the coloration model. Can't be null.
     * 
     * @param colorationModel
     * 
     * @throws NullPointerException
     *             if the colorationModel is null.
     */
    public void setColorationModel(ColorationModel colorationModel)
    {
        if (colorationModel == null)
        {
            throw new NullPointerException("The coloration model can't be null.");
        }
        this.colorationModel = colorationModel;
    }
    
}
