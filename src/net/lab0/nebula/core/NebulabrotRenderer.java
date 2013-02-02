package net.lab0.nebula.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.EventListenerList;

import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.listener.MandelbrotRendererListener;
import net.lab0.tools.geom.RectangleInterface;

/**
 * 
 * Class for rendering the nebulabrot fractal
 * 
 * @author 116
 * 
 */
public class NebulabrotRenderer
{
    /**
     * the width of the rendering
     */
    private int                pixelWidth;
    /**
     * the height of the rendering
     */
    private int                pixelHeight;
    /**
     * the viewport of the rendering
     */
    private RectangleInterface viewPort;
    /**
     * the event listeners
     */
    private EventListenerList  eventListenerList = new EventListenerList();
    /**
     * utilized to force the exit of a rendering loop
     */
    private boolean            stopAndExit;
    
    /**
     * 
     * @param pixelWidth
     *            the width of the rendering
     * @param pixelHeight
     *            the height of the rendering
     * @param viewPort
     *            the viewport of the rendering
     */
    public NebulabrotRenderer(int pixelWidth, int pixelHeight, RectangleInterface viewPort)
    {
        super();
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        this.viewPort = viewPort;
    }
    
    public void addMandelbrotRendererListener(MandelbrotRendererListener listener)
    {
        eventListenerList.add(MandelbrotRendererListener.class, listener);
    }
    
    private void fireProgress(long current, long total)
    {
        for (MandelbrotRendererListener listener : eventListenerList.getListeners(MandelbrotRendererListener.class))
        {
            listener.rendererProgress(current, total);
        }
    }
    
    private void fireFinished(RawMandelbrotData data)
    {
        for (MandelbrotRendererListener listener : eventListenerList.getListeners(MandelbrotRendererListener.class))
        {
            listener.rendererFinished(data);
        }
    }
    
    private void fireStopped(RawMandelbrotData data)
    {
        for (MandelbrotRendererListener listener : eventListenerList.getListeners(MandelbrotRendererListener.class))
        {
            listener.rendererStopped(data);
        }
    }
    
    private void fireFinishedOrStop(RawMandelbrotData raw)
    {
        if (stopAndExit)
        {
            fireStopped(raw);
        }
        else
        {
            fireFinished(raw);
        }
    }
    
    /**
     * Basic and naive method to render the nebulabrot. Does a rendering of the nebulabrot using start points dispatched on a grid.
     * 
     * @param pointsCount
     *            the number of points on the grid
     * @param minIter
     *            the minimum number of iterations the points on the grid need to have to be utilized
     * @param maxIter
     *            the maximum number of iterations the points on the grid need to have to be utilized
     * @return a {@link RawMandelbrotData} of the computed points
     */
    public RawMandelbrotData linearRender(long pointsCount, int minIter, int maxIter)
    {
        // wouldn't make sens otherwise (no point computed)
        assert (minIter < maxIter);
        
        RawMandelbrotData raw = new RawMandelbrotData(pixelWidth, pixelHeight, minIter, maxIter, pointsCount);
        int[][] data = raw.getData();
        
        int side = (int) Math.sqrt(pointsCount);
        double stepX = viewPort.getWidth() / side;
        double stepY = viewPort.getHeight() / side;
        
        // label set here to exit the main loop is case of stop request
        exit:
        {
            for (int x = 0; x < side; ++x)
            {
                fireProgress(x, side);
                for (int y = 0; y < side; ++y)
                {
                    double real = viewPort.getCenter().getX() - viewPort.getWidth() / 2 + x * stepX;
                    double img = viewPort.getCenter().getY() - viewPort.getHeight() / 2 + y * stepY;
                    
                    // we only want to use points outside the mandelbrot set
                    if (isOutsideMandelbrotSet(real, img, maxIter))
                    {
                        if (stopAndExit)
                        {
                            break exit;
                        }
                        
                        computePoint(minIter, maxIter, data, real, img);
                    }
                }
            }
        }
        
        fireFinishedOrStop(raw);
        return raw;
    }
    
    /**
     * computation for 1 point
     * 
     * @param minIter
     * @param maxIter
     * @param data
     *            the data array to save the results to
     * @param real
     *            real part of the point
     * @param img
     *            imaginary part of the point
     */
    private void computePoint(int minIter, int maxIter, int[][] data, double real, double img)
    {
        double realsqr = real * real;
        double imgsqr = img * img;
        
        double real1 = real;
        double img1 = img;
        double real2, img2;
        
        // reach the minimum iteration count without rendering anything
        int iter = 0;
        while ((iter < minIter) && ((realsqr + imgsqr) < 4))
        {
            real2 = real1 * real1 - img1 * img1 + real;
            img2 = 2 * real1 * img1 + img;
            
            real1 = real2 * real2 - img2 * img2 + real;
            img1 = 2 * real2 * img2 + img;
            
            realsqr = real2 * real2;
            imgsqr = img2 * img2;
            real1 = realsqr - imgsqr + real;
            img1 = 2 * real2 * img2 + img;
            
            iter += 2;
        }
        
        // starts the rendering
        while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
        {
            int X = getXValue(real1);
            int Y = getYValue(img1);
            // check if inside the rendering area
            if (X >= 0 && X < pixelWidth && Y >= 0 && Y < pixelHeight)
            {
                data[X][Y]++;// TODO : optimize awful increment if possible
            }
            
            real2 = real1 * real1 - img1 * img1 + real;
            img2 = 2 * real1 * img1 + img;
            
            realsqr = real2 * real2;
            imgsqr = img2 * img2;
            real1 = real2;
            img1 = img2;
            
            iter++;
        }
        
        // System.out.println("Point ;" + iter + ";" + real + ";+i;" + img);
    }
    
    /**
     * 
     * Evolved method to render the nebulabrot. Does a rendering of the nebulabrot using start points dispatched on a grid. If the points of this grid do not
     * need to be computed, they are discarded and considered computed. This is made to ensure equivalence of this parameter with the parameter of the naive
     * rendering method
     * 
     * @param pointsCount
     *            the number of points on the grid
     * @param minIter
     *            the minimum number of iterations the points on the grid need to have to be utilized
     * @param maxIter
     *            the maximum number of iterations the points on the grid need to have to be utilized
     * @param root
     *            the root, a {@link QuadTreeNode} to the root of the tree which must be utilized to render the fractal
     * @return a {@link RawMandelbrotData} of the computed points
     */
    public RawMandelbrotData quadTreeRender(long pointsCount, int minIter, int maxIter, QuadTreeNode root)
    {
        RawMandelbrotData raw = new RawMandelbrotData(pixelWidth, pixelHeight, minIter, maxIter, pointsCount);
        int[][] data = raw.getData();
        
        long side = (long) Math.sqrt(pointsCount);
        double stepX = viewPort.getWidth() / side;
        double stepY = viewPort.getHeight() / side;
        
        List<QuadTreeNode> nodesList = new ArrayList<>();
        root.getLeafNodes(nodesList, Arrays.asList(Status.BROWSED, Status.OUTSIDE, Status.VOID));
        
        double workSurface = 0;
        double browsedSurface = 0;
        for (QuadTreeNode n : nodesList)
        {
            workSurface += n.getSurface();
            if (n.status == Status.BROWSED)
            {
                browsedSurface += n.getSurface();
            }
        }
        System.out.println("work surface = " + workSurface);
        System.out.println("browsed surface = " + browsedSurface);
        
        // double discardedSurface = 0;
        long current = 0;
        // label set here to exit the main loop is case of stop request
        exit:
        {
            for (QuadTreeNode node : nodesList)
            {
                current++;
                fireProgress(current, nodesList.size());
                
                if (node.min <= maxIter || node.max >= minIter)
                {
                    double xStart = Math.floor(Math.abs(node.minX) / stepX) * stepX;
                    double yStart = Math.floor(Math.abs(node.minY) / stepY) * stepY;
                    
                    if (Math.IEEEremainder(node.minX, stepX) != 0)
                    {
                        if (node.minX < 0)
                        {
                            xStart = -xStart;
                        }
                        else
                        {
                            xStart += stepX;
                        }
                    }
                    
                    if (Math.IEEEremainder(node.minY, stepY) != 0)
                    {
                        if (node.minY < 0)
                        {
                            yStart = -yStart;
                        }
                        else
                        {
                            yStart += stepY;
                        }
                    }
                    
                    double x = xStart;
                    
                    while (x < node.maxX)
                    {
                        double y = yStart;
                        while (y < node.maxY)
                        {
                            double real = x;
                            double img = y;
                            
                            // if the node is inside and the number of iterations match the requirements
                            if ((node.status == Status.OUTSIDE && (node.min < maxIter || node.max > minIter))
                            // or if the point is outside
                            || isOutsideMandelbrotSet(real, img, maxIter))
                            {
                                if (stopAndExit)
                                {
                                    break exit;
                                }
                                
                                computePoint(minIter, maxIter, data, real, img);
                            }
                            
                            y += stepY;
                        }
                        
                        x += stepX;
                    }
                }
            }
        }
        
        fireFinishedOrStop(raw);
        return raw;
    }
    
    /**
     * 
     * @param real
     * @param img
     * @param maxIter
     * @return <code>true</code> if the point is inside the mandelbrot set, <code>false</code> otherwise
     */
    private boolean isOutsideMandelbrotSet(double real, double img, int maxIter)
    {
        double realsqr = real * real;
        double imgsqr = img * img;
        
        double real1 = real;
        double img1 = img;
        double real2, img2;
        
        int iter = 0;
        while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
        {
            real2 = real1 * real1 - img1 * img1 + real;
            img2 = 2 * real1 * img1 + img;
            
            real1 = real2 * real2 - img2 * img2 + real;
            img1 = 2 * real2 * img2 + img;
            
            realsqr = real2 * real2;
            imgsqr = img2 * img2;
            real1 = realsqr - imgsqr + real;
            img1 = 2 * real2 * img2 + img;
            
            iter += 2;
        }
        
        return iter < maxIter;
    }
    
    private int getYValue(double imaginary)
    {
        double pxlHeight = (double) pixelHeight;
        
        double originY = viewPort.getCenter().getY(); // origin
        double viewportHeight = viewPort.getHeight();
        
        return (int) ((imaginary - originY + viewportHeight / 2) * pxlHeight / viewportHeight);
    }
    
    private int getXValue(double real)
    {
        double pxlWidth = (double) pixelWidth;
        
        double originX = viewPort.getCenter().getX(); // origin
        double viewportWidth = viewPort.getWidth();
        
        return (int) ((real - originX + viewportWidth / 2) * pxlWidth / viewportWidth);
    }
    
    public void stopAndExit()
    {
        stopAndExit = true;
    }
}
