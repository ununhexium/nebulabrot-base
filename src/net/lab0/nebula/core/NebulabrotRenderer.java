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

public class NebulabrotRenderer
{
    private int                pixelWidth;
    private int                pixelHeight;
    private RectangleInterface viewPort;
    private EventListenerList  eventListenerList = new EventListenerList();
    private boolean            stopAndExit;
    
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
    
    public RawMandelbrotData linearRender(long pointsCount, int minIter, int maxIter)
    {
        assert (minIter < maxIter);
        
        RawMandelbrotData raw = new RawMandelbrotData(pixelWidth, pixelHeight);
        int[][] data = raw.getData();
        
        int side = (int) Math.sqrt(pointsCount);
        double stepX = viewPort.getWidth() / side;
        double stepY = viewPort.getHeight() / side;
        
        exit:
        {
            for (int x = 0; x < side; ++x)
            {
                fireProgress(x, side);
                for (int y = 0; y < side; ++y)
                {
                    double real = viewPort.getCenter().getX() - viewPort.getWidth() / 2 + x * stepX;
                    double img = viewPort.getCenter().getY() - viewPort.getHeight() / 2 + y * stepY;
                    
                    double realsqr = real * real;
                    double imgsqr = img * img;
                    
                    double real1 = real;
                    double img1 = img;
                    double real2, img2;
                    
                    if (isOutsideMandelbrotSet(real, img, maxIter))
                    {
                        if (stopAndExit)
                        {
                            break exit;
                        }
                        
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
                        
                        while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
                        {
                            int X = getXValue(real1);
                            int Y = getYValue(img1);
                            if (X >= 0 && X < pixelWidth && Y >= 0 && Y < pixelHeight)
                            {
                                data[X][Y]++;
                            }
                            // System.out.println("real(" + X + ")=" + real1 +
                            // " img(" + Y + ")=" + img1 + " iter=" + iter);
                            
                            real2 = real1 * real1 - img1 * img1 + real;
                            img2 = 2 * real1 * img1 + img;
                            
                            realsqr = real2 * real2;
                            imgsqr = img2 * img2;
                            real1 = real2;
                            img1 = img2;
                            
                            iter++;
                        }
                    }
                }
            }
        }
        
        fireFinishedOrStop(raw);
        return raw;
    }
    
    public RawMandelbrotData quadTreeRender(long pointsCount, int minIter, int maxIter, QuadTreeNode root)
    {
        RawMandelbrotData raw = new RawMandelbrotData(pixelWidth, pixelHeight);
        int[][] data = raw.getData();
        
        long side = (long) Math.sqrt(pointsCount);
        double stepX = viewPort.getWidth() / side;
        double stepY = viewPort.getHeight() / side;
        System.out.println("stepX=" + stepX);
        
        List<QuadTreeNode> nodesList = new ArrayList<>();
        root.getLeafNodes(nodesList, Arrays.asList(Status.BROWSED, Status.OUTSIDE, Status.VOID));
        
        System.out.println("Nodes count: " + nodesList.size());
        
        long current = 0;
        exit:
        {
            for (QuadTreeNode node : nodesList)
            {
                current++;
                fireProgress(current, nodesList.size());
                
                if (node.min <= maxIter || node.max >= minIter)
                {
                    
                    double xStart = Math.floor(node.minX / stepX) * stepX + stepX;
                    double yStart = Math.floor(node.minY / stepY) * stepY + stepY;
                    // System.out.println(stepX + "/" + (node.maxX - node.minX) + " xLen=" + xLen + ", yLen=" + yLen);
                    
                    double x = xStart;
                    
                    while (x < node.maxX)
                    {
                        double y = yStart;
                        while (y < node.maxY)
                        {
                            double real = x;
                            double img = y;
                            
                            double realsqr = real * real;
                            double imgsqr = img * img;
                            
                            double real1 = real;
                            double img1 = img;
                            double real2, img2;
                            
                            if (node.status == Status.OUTSIDE || isOutsideMandelbrotSet(real, img, maxIter))
                            {
                                if (stopAndExit)
                                {
                                    break exit;
                                }
                                
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
                                
                                while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
                                {
                                    int X = getXValue(real1);
                                    int Y = getYValue(img1);
                                    if (X >= 0 && X < pixelWidth && Y >= 0 && Y < pixelHeight)
                                    {
                                        data[X][Y]++;
                                    }
                                    // System.out.println("real(" + X + ")=" + real1 +
                                    // " img(" + Y + ")=" + img1 + " iter=" + iter);
                                    
                                    real2 = real1 * real1 - img1 * img1 + real;
                                    img2 = 2 * real1 * img1 + img;
                                    
                                    realsqr = real2 * real2;
                                    imgsqr = img2 * img2;
                                    real1 = real2;
                                    img1 = img2;
                                    
                                    iter++;
                                }
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
