
package net.lab0.nebula.core;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.EventListenerList;

import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.listener.MandelbrotRendererListener;
import net.lab0.tools.geom.Rectangle;


public class MandelbrotRenderer
{
    private int               pixelWidth;
    private int               pixelHeight;
    private Rectangle         viewPort;
    private EventListenerList eventListenerList = new EventListenerList();
    
    public MandelbrotRenderer(int pixelWidth, int pixelHeight, Rectangle viewPort)
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
            listener.progress(current, total);
        }
    }
    
    private void fireFinished(RawMandelbrotData data)
    {
        for (MandelbrotRendererListener listener : eventListenerList.getListeners(MandelbrotRendererListener.class))
        {
            listener.finished(data);
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
                        if (X >= 0 && X < pixelWidth && Y >= 0 && Y < pixelWidth)
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
        
        fireFinished(raw);
        return raw;
    }
    
    public RawMandelbrotData quadTreeRender(long pointsCount, int minIter, int maxIter, QuadTreeNode root)
    {
        assert (minIter < maxIter);
        
        RawMandelbrotData raw = new RawMandelbrotData(pixelWidth, pixelHeight);
        int[][] data = raw.getData();
        
        long side = (long) Math.sqrt(pointsCount);
        double stepX = viewPort.getWidth() / side;
        double stepY = viewPort.getHeight() / side;
        System.out.println("stepX=" + stepX);
        
        List<QuadTreeNode> nodesList = new ArrayList<>();
        root.getLeafNodes(nodesList, Arrays.asList(Status.BROWSED, Status.OUTSIDE, Status.VOID));
        
        System.out.println("Nodes: " + nodesList.size());
        
        long current = 0;
        for (QuadTreeNode node : nodesList)
        {
            current++;
            fireProgress(current, nodesList.size());
            
            if ((node.status == Status.BROWSED && !node.hasComputedChildren() && node.min <= maxIter)
            || (node.status == Status.OUTSIDE && (node.min <= maxIter || node.max >= minIter)))
            {
                // System.out.println(stepX+"/"+(node.maxX - node.minX));
                int xLen = (int) ((node.maxX - node.minX) / stepX);
                int yLen = (int) ((node.maxY - node.minY) / stepY);
                
                for (int x = 0; x < xLen; ++x)
                {
                    for (int y = 0; y < yLen; ++y)
                    {
                        double real = node.minX + x * stepX;
                        double img = node.minY + y * stepY;
                        
                        double realsqr = real * real;
                        double imgsqr = img * img;
                        
                        double real1 = real;
                        double img1 = img;
                        double real2, img2;
                        
                        if (node.status == Status.OUTSIDE || isOutsideMandelbrotSet(real, img, maxIter))
                        {
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
                                // System.out.println("real(" + X + ")=" + real1
                                // +
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
        }
        
        fireFinished(raw);
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
}