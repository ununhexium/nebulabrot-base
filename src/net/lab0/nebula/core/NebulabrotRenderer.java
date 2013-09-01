package net.lab0.nebula.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;

import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.listener.MandelbrotRendererListener;
import net.lab0.tools.HumanReadable;
import net.lab0.tools.geom.RectangleInterface;

import org.tukaani.xz.XZInputStream;

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
     * The width of the rendering.
     */
    private int                pixelWidth;
    
    /**
     * The height of the rendering.
     */
    private int                pixelHeight;
    
    /**
     * The viewport of the rendering.
     */
    private RectangleInterface viewPort;
    
    /**
     * The event listeners.
     */
    private EventListenerList  eventListenerList = new EventListenerList();
    
    /**
     * Utilized to force the exit of a rendering loop.
     */
    private boolean            stopAndExit;
    
    /**
     * 
     * @param pixelWidth
     *            The width of the rendering.
     * @param pixelHeight
     *            The height of the rendering.
     * @param viewPort
     *            The viewport of the rendering.
     */
    public NebulabrotRenderer(int pixelWidth, int pixelHeight, RectangleInterface viewPort)
    {
        super();
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        this.viewPort = viewPort;
    }
    
    /**
     * 
     * @param listener
     *            The listener to add to <code>this</code>.
     */
    public void addMandelbrotRendererListener(MandelbrotRendererListener listener)
    {
        eventListenerList.add(MandelbrotRendererListener.class, listener);
    }
    
    /**
     * Fires a <code>rendererProgress</code> event to all the registered {@link MandelbrotRendererListener}.
     * 
     * @param current
     *            The current advancement in the rendering.
     * @param total
     *            The advancement to reach.
     */
    public void fireProgress(long current, long total)
    {
        for (MandelbrotRendererListener listener : eventListenerList.getListeners(MandelbrotRendererListener.class))
        {
            listener.rendererProgress(current, total);
        }
    }
    
    /**
     * Fires a <code>rendererFinished</code> event to all the registered {@link MandelbrotRendererListener}.
     * 
     * @param data
     *            The resulting {@link RawMandelbrotData}.
     */
    private void fireFinished(RawMandelbrotData data)
    {
        for (MandelbrotRendererListener listener : eventListenerList.getListeners(MandelbrotRendererListener.class))
        {
            listener.rendererFinished(data);
        }
    }
    
    /**
     * Fires a <code>rendererStopped</code> event to all the registered {@link MandelbrotRendererListener}.
     * 
     * @param data
     *            The resulting {@link RawMandelbrotData} computed do far.
     */
    private void fireStopped(RawMandelbrotData data)
    {
        for (MandelbrotRendererListener listener : eventListenerList.getListeners(MandelbrotRendererListener.class))
        {
            listener.rendererStopped(data);
        }
    }
    
    /**
     * Fires a <code>rendererStopped</code> or <code>rendererFinished</code> to event to all the registered
     * {@link MandelbrotRendererListener}, depending on the terminating action.
     * 
     * @param raw
     */
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
     * Basic and naive method to render the nebulabrot. Does a rendering of the nebulabrot using start points dispatched
     * on a grid.
     * 
     * @param pointsCount
     *            The number of points on the grid.
     * @param minIter
     *            The minimum number of iterations the points on the grid need to have to be utilized.
     * @param maxIter
     *            The maximum number of iterations the points on the grid need to have to be utilized.
     * @param thread
     *            The maximum number of threads to use for this computation.
     * 
     * @return a {@link RawMandelbrotData} of the computed points
     * 
     * @throws IllegalArgumentException
     *             if <code>minIter >= maxIter</code> is true
     */
    public RawMandelbrotData linearRender(long pointsCount, final int minIter, final int maxIter, int threads)
    {
        // wouldn't make sens otherwise (no point computed)
        if (minIter >= maxIter)
        {
            throw new IllegalArgumentException("minIter must be strictly inferior to maxIter");
        }
        
        RawMandelbrotData raw = new RawMandelbrotData(pixelWidth, pixelHeight, pointsCount);
        final int[][] data = raw.getData();
        
        // TODO : fins a better way to compute side parameter, based of the view port ratio
        final int side = (int) Math.sqrt(pointsCount);
        final double stepX = viewPort.getWidth() / side;
        final double stepY = viewPort.getHeight() / side;
        
        final int queueLimit = 1024;
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueLimit);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(threads, threads, 500, TimeUnit.MILLISECONDS, queue);
        
        // filler thread
        Thread thread = new Thread("Filler thread")
        {
            @Override
            public void run()
            {
                for (int x = 0; x <= side; ++x)
                {
                    final int finalX = x;
                    Runnable runnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (stopAndExit)
                            {
                                return;
                            }
                            
                            for (int y = 0; y <= side; ++y)
                            {
                                double real = viewPort.getCenter().getX() - viewPort.getWidth() / 2 + finalX * stepX;
                                double img = viewPort.getCenter().getY() - viewPort.getHeight() / 2 + y * stepY;
                                
                                // we only want to use points outside the mandelbrot set
                                if (MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim2(real, img, maxIter))
                                {
                                    if (stopAndExit)
                                    {
                                        break;
                                    }
                                    
                                    computePoint(minIter, maxIter, data, real, img);
                                }
                            }
                            
                            fireProgress(finalX, side);
                        }
                    };
                    
                    while (executor.getQueue().size() >= queueLimit)
                    {
                        try
                        {
                            Thread.sleep(250);
                        }
                        catch (InterruptedException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    executor.execute(runnable);
                }
                
                executor.shutdown();
                boolean done = false;
                while (!done)
                {
                    try
                    {
                        done = executor.awaitTermination(250, TimeUnit.MILLISECONDS);
                        // System.out.println("wait for " + queue.size() + ", running " + executor.getActiveCount());
                        fireProgress(queue.size(), side);
                    }
                    catch (InterruptedException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        
        thread.start();
        try
        {
            thread.join();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        fireFinishedOrStop(raw);
        return raw;
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
    private int computePoint(int minIter, int maxIter, int[][] data, double real, double img)
    {
        double realsqr = real * real;
        double imgsqr = img * img;
        
        double real1 = real;
        double img1 = img;
        double real2 = real;
        double img2 = img;
        
        // System.out.println("Compute : " + real + "+j" + img);
        
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
        
        double pxlHeight = (double) pixelHeight;
        double pxlWidth = (double) pixelWidth;
        double originY = viewPort.getCenter().getY(); // origin
        double originX = viewPort.getCenter().getX(); // origin
        double viewportHeight = viewPort.getHeight();
        double viewportWidth = viewPort.getWidth();
        
        // starts the rendering
        int increment = 0;
        while ((iter < maxIter) && ((real2 * real2 + img2 * img2) < 4))
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
    
    /**
     * 
     * Evolved method to render the nebulabrot. Does a rendering of the nebulabrot using start points dispatched on a
     * grid. If the points of this grid do not need to be computed, they are discarded and considered computed. This is
     * made to ensure equivalence of this parameter with the parameter of the naive rendering method.
     * 
     * @param pointsCount
     *            the number of points on the grid
     * @param minIter
     *            the minimum number of iterations the points on the grid need to have to be utilized
     * @param maxIter
     *            the maximum number of iterations the points on the grid need to have to be utilized
     * @param root
     *            the root, a {@link StatusQuadTreeNode} to the root of the tree which must be utilized to render the
     *            fractal
     * @param threads
     *            The maximum number of threads to use for this computation.
     * 
     * @return a {@link RawMandelbrotData} of the computed points
     * 
     * @throws IllegalArgumentException
     *             if <code>minIter >= maxIter</code> is true
     */
    public RawMandelbrotData quadTreeRender(long pointsCount, final int minIter, final int maxIter,
    StatusQuadTreeNode root, int threads)
    {
        // wouldn't make sens otherwise (no point computed)
        if (minIter >= maxIter)
        {
            throw new IllegalArgumentException("minIter must be strictly inferior to maxIter");
        }
        
        RawMandelbrotData raw = new RawMandelbrotData(pixelWidth, pixelHeight, pointsCount);
        System.out.println("malloc");
        final int[][] data = raw.getData();
        
        final long side = Math.round(Math.sqrt(pointsCount));
        // TODO : better method to dispatch points between X and Y
        final double stepX = viewPort.getWidth() / side;
        final double stepY = viewPort.getHeight() / side;
        
        System.out.println("get nodes");
        // get the appropriate nodes
        final List<StatusQuadTreeNode> nodesList = new ArrayList<>();
        root.getLeafNodes(nodesList, Arrays.asList(Status.BROWSED, Status.OUTSIDE, Status.VOID), minIter, maxIter);
        
        // final List<Pair<StatusQuadTreeNode, Integer>> frequency = Collections.synchronizedList(new
        // ArrayList<Pair<StatusQuadTreeNode, Integer>>());
        
        final int queueLimit = 1024;
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueLimit);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(threads, threads, 500, TimeUnit.MILLISECONDS, queue);
        
        // filler thread
        Thread thread = new Thread("Filler thread")
        {
            @Override
            public void run()
            {
                int current = 0;
                for (StatusQuadTreeNode node : nodesList)
                {
                    current++;
                    final int currentFinal = current;
                    final StatusQuadTreeNode finalNode = node;
                    
                    Runnable runnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (stopAndExit)
                            {
                                return;
                            }
                            
                            // find the first point inside the node
                            
                            double xStart = Math.ceil(finalNode.getMinX() / stepX) * stepX;
                            double yStart = Math.ceil(finalNode.getMinY() / stepY) * stepY;
                            
                            // System.out.println("start1 (" + xStart + ";" + yStart + ")");
                            
                            double real = xStart;
                            double maxX = finalNode.getMaxX();
                            double maxY = finalNode.getMaxY();
                            
                            while (real < maxX)
                            {
                                double img = yStart;
                                while (img < maxY)
                                {
                                    // if the point is outside
                                    if (MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim2(real, img, maxIter))
                                    {
                                        if (stopAndExit)
                                        {
                                            return;
                                        }
                                        
                                        int increments = computePoint(minIter, maxIter, data, real, img);
                                        // frequency.add(new Pair<StatusQuadTreeNode, Integer>(finalNode, increments));
                                    }
                                    
                                    img += stepY;
                                }
                                
                                real += stepX;
                            }
                            
                            fireProgress(currentFinal, nodesList.size());
                        }
                    };
                    while (executor.getQueue().size() >= queueLimit)
                    {
                        try
                        {
                            Thread.sleep(250);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    executor.execute(runnable);
                    
                }
                
                executor.shutdown();
                boolean done = false;
                while (!done)
                {
                    try
                    {
                        done = executor.awaitTermination(250, TimeUnit.MILLISECONDS);
                        // System.out.println("wait for " + queue.size() + ", running " + executor.getActiveCount());
                        fireProgress(queue.size(), side);
                    }
                    catch (InterruptedException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        
        thread.start();
        try
        {
            thread.join();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Collections.sort(frequency, new Comparator<Pair<StatusQuadTreeNode, Integer>>()
        // {
        // @Override
        // public int compare(Pair<StatusQuadTreeNode, Integer> o1, Pair<StatusQuadTreeNode, Integer> o2)
        // {
        // return o2.b - o1.b;
        // }
        // });
        
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        long total = 0;
        // for (Pair<StatusQuadTreeNode, Integer> p : frequency)
        // {
        // total += p.b;
        // if (p.b > max)
        // {
        // max = p.b;
        // }
        // if (p.b < min)
        // {
        // min = p.b;
        // }
        // }
        
        System.out.println("min=" + min + " max=" + max);
        int dX = 1; // decile
        int index = 0;
        long cumul = 0;
        // for (Pair<StatusQuadTreeNode, Integer> p : frequency)
        // {
        // cumul += p.b;
        // index++;
        // if (cumul >= (dX * total / 20.0d))
        // {
        // System.out.println("D" + dX + " @ " + index);
        // dX++;
        // }
        // }
        
        fireFinishedOrStop(raw);
        return raw;
    }
    
    /**
     * 
     * Does a rendering of the nebulabrot using points indicated in a file.
     * 
     * @param inputFile
     *            The file to read for the computation. The file must be in binary format. The first 8 bytes represent a
     *            <code>long</code> indicating how many points were computed. Then the file is composed of blocks {int
     *            iterations, double xCoordinate, double yCoordinate} representing the coordinates to compute and how
     *            many iterations are needed.
     * @param minIter
     *            the minimum number of iterations the points on the grid need to have to be used
     * @param maxIter
     *            the maximum number of iterations the points on the grid need to have to be used
     * 
     * @return a {@link RawMandelbrotData} of the computed points
     * @throws IOException
     *             if there is a problem while reading the file.
     */
    public RawMandelbrotData fileRender(File inputFile, int minIter, int maxIter)
    throws IOException
    {
        try (
            XZInputStream inputStream = new XZInputStream(new FileInputStream(inputFile)))
        {
            RawMandelbrotData raw = new RawMandelbrotData(pixelWidth, pixelHeight, 0);
            
            System.out.println("Malloc");
            final int[][] data = raw.getData();
            
            byte[] buffer = new byte[4 + 8 + 8];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            
            System.out.println("Reading");
            long read = 0;
            long totalRead = 0;
            long lastTime = System.currentTimeMillis();
            while ((read = inputStream.read(buffer)) != -1)
            {
                totalRead += read;
                if (System.currentTimeMillis() > lastTime + TimeUnit.SECONDS.toMillis(2))
                {
                    lastTime += TimeUnit.SECONDS.toMillis(2);
                    System.out.println("Read " + HumanReadable.humanReadableNumber(totalRead));
                }
                int iter = byteBuffer.getInt();
                double real = byteBuffer.getDouble();
                double img = byteBuffer.getDouble();
                int localMaxIter = Math.min(maxIter, iter);
                byteBuffer.clear();
                computePoint(minIter, localMaxIter, data, real, img);
            }
            System.out.println("Read all the file");
            
            fireFinishedOrStop(raw);
            return raw;
        }
    }
    
    /**
     * requests the computation to stop and return
     */
    public void stopAndExit()
    {
        stopAndExit = true;
    }
    
    /**
     * 
     * @return <code>true</code> if it was requested to stop the computation, <code>false</code> otherwise.
     */
    public boolean isStopAndExit()
    {
        return stopAndExit;
    }
}
