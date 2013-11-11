package net.lab0.nebula.exe;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.tools.exec.AggregatorJob;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.geom.RectangleInterface;

public class NebulaAggregator
extends AggregatorJob<PointsBlock, RawMandelbrotData>
{
    /**
     * The height of the rendering.
     */
    private int                pixelHeight;
    
    /**
     * The width of the rendering.
     */
    private int                pixelWidth;
    
    /**
     * The viewport of the rendering.
     */
    private RectangleInterface viewPort;
    
    /**
     * Points with this amount of iterations or below will not be rendered.
     */
    private long               minimumIteration;
    
    /**
     * Points with this amount of iterations or over will not be rendered.
     */
    private long               maximumIteration;
    
    public NebulaAggregator(PriorityExecutor executor, int priority, PointsBlock input, RawMandelbrotData aggregate,
    RectangleInterface viewPort, long minimumIteration, long maximumIteration)
    {
        super(executor, priority, input, aggregate);
        this.pixelHeight = aggregate.getPixelHeight();
        this.pixelWidth = aggregate.getPixelWidth();
        this.viewPort = viewPort;
        this.maximumIteration = maximumIteration;
        this.minimumIteration = minimumIteration;
    }
    
    @Override
    public void aggregate(PointsBlock input)
    {
        int[][] data = aggregate.getData();
        long min = input.iter[0];
        long max = input.iter[0];
        for (int i = 0; i < input.size; i++)
        {
            if (input.iter[i] < min)
            {
                min = input.iter[i];
            }
            else if (input.iter[i] > max)
            {
                max = input.iter[i];
            }
            if (input.iter[i] > minimumIteration && input.iter[i] < maximumIteration)
            {
                computePoint(input.iter[i], data, input.real[i], input.imag[i]);
            }
        }
        input.release();
    }
    
    /**
     * Rendering on a 2D surface for 1 point.
     * 
     * @param minIter
     *            The minimum iteration count to reach before doing the rendering with the remaining iterations.
     * @param maxIter
     *            The maximum iteration count.
     * @param data
     *            The data array to save the results to.
     * @param real
     *            The real part of the point.
     * @param img
     *            The imaginary part of the point.
     * 
     * @return the number of increment done when computing this point
     */
    private long computePoint(long maxIter, int[][] data, double real, double img)
    {
        double real1 = real;
        double img1 = img;
        double real2 = real;
        double img2 = img;
        
        double pxlHeight = (double) pixelHeight;
        double pxlWidth = (double) pixelWidth;
        double originY = viewPort.getCenter().getY(); // origin
        double originX = viewPort.getCenter().getX(); // origin
        double viewportHeight = viewPort.getHeight();
        double viewportWidth = viewPort.getWidth();
        
        // starts the rendering
        long iter = 0;
        long increment = 0;
        while (iter < maxIter)
        {
            int X = (int) ((real1 - originX + viewportWidth / 2) * pxlWidth / viewportWidth);
            int Y = (int) ((img1 - originY + viewportHeight / 2) * pxlHeight / viewportHeight);
            // check if inside the rendering area
            if (X >= 0 && X < pixelWidth && Y >= 0 && Y < pixelHeight)
            {
                data[X][Y]++;
                increment++;
            }
            
            real2 = real1 * real1 - img1 * img1 + real;
            img2 = 2 * real1 * img1 + img;
            
            real1 = real2;
            img1 = img2;
            
            iter++;
        }
        
        return increment;
    }
}
