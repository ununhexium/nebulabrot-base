package net.lab0.nebula;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import net.lab0.nebula.color.GrayScaleColorModel;
import net.lab0.nebula.core.NebulabrotRenderer;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.listener.ConsoleMandelbrotRendererListener;
import net.lab0.tools.geom.RectangleInterface;

import org.apache.commons.lang3.time.StopWatch;

/**
 * Thread used in the performance test class.
 * 
 * @author 116@lab0.net
 * 
 */
public class TestingThread
extends Thread
{
    private StatusQuadTreeNode quadTreeRoot;
    private int                xRes;
    private RectangleInterface viewport;
    private long               pointsCount;
    private int                minIter;
    private int                maxIter;
    private Path               savePath;
    
    private NebulabrotRenderer renderer;
    private RawMandelbrotData  data;
    
    public TestingThread(StatusQuadTreeNode quadTreeRoot, int xRes, RectangleInterface viewport, long pointsCount, int minIter, int maxIter, Path savePath)
    {
        super();
        this.quadTreeRoot = quadTreeRoot;
        this.xRes = xRes;
        this.viewport = viewport;
        this.pointsCount = pointsCount;
        this.minIter = minIter;
        this.maxIter = maxIter;
        this.savePath = savePath;
    }
    
    @Override
    public void run()
    {
        try
        {
            data = testQuadTreeRenderingSpeed(quadTreeRoot, xRes, viewport, pointsCount, minIter, maxIter, savePath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void stopTest()
    {
        renderer.stopAndExit();
    }
    
    public RawMandelbrotData getData()
    {
        return data;
    }
    
    private RawMandelbrotData testQuadTreeRenderingSpeed(StatusQuadTreeNode quadTreeRoot, int xRes, RectangleInterface viewport, long pointsCount, int minIter,
    int maxIter, Path savePath)
    throws IOException
    {
        System.out.println("quad");
        int yRes = (int) (xRes / viewport.getWidth() * viewport.getHeight());
        
        renderer = new NebulabrotRenderer(xRes, yRes, viewport);
        renderer.addMandelbrotRendererListener(new ConsoleMandelbrotRendererListener(0.1));
        int threads = Runtime.getRuntime().availableProcessors() - 1;
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        RawMandelbrotData rawMandelbrotData = renderer.quadTreeRender(pointsCount, minIter, maxIter, quadTreeRoot, threads);
        stopWatch.stop();
        
        rawMandelbrotData.addAdditionnalInformation("rendering.minIter", Integer.toString(minIter));
        rawMandelbrotData.addAdditionnalInformation("rendering.maxIter", Integer.toString(maxIter));
        rawMandelbrotData.addAdditionnalInformation("rendering.method", "quadTree");
        rawMandelbrotData.addAdditionnalInformation("rendering.time", Double.toString(stopWatch.getNanoTime() / 1_000_000_000.0d));
        BufferedImage image = rawMandelbrotData.computeBufferedImage(new GrayScaleColorModel(), 4);
        rawMandelbrotData.save(savePath);
        ImageIO.write(image, "PNG", new File(savePath.toFile(), "preview.png"));
        
        return rawMandelbrotData;
    }
    
    private RawMandelbrotData testLinearRenderingSpeed(int xRes, RectangleInterface viewport, long pointsCount, int minIter, int maxIter, Path savePath)
    throws IOException
    {
        System.out.println("lin");
        int yRes = (int) (xRes / viewport.getWidth() * viewport.getHeight());
        
        renderer = new NebulabrotRenderer(xRes, yRes, viewport);
        // renderer.addMandelbrotRendererListener(new ConsoleMandelbrotRendererListener());
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        RawMandelbrotData rawMandelbrotData = renderer.linearRender(pointsCount, minIter, maxIter, 1);
        stopWatch.stop();
        
        rawMandelbrotData.addAdditionnalInformation("rendering.method", "linear");
        rawMandelbrotData.addAdditionnalInformation("rendering.time", Double.toString(stopWatch.getNanoTime() / 1_000_000_000.0d));
        rawMandelbrotData.save(savePath);
        
        return rawMandelbrotData;
    }
}