package net.lab0.nebula;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import net.lab0.nebula.color.GrayScaleColorModel;
import net.lab0.nebula.core.NebulabrotRenderer;
import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.listener.ConsoleMandelbrotRendererListener;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import net.lab0.tools.geom.RectangleInterface;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.lang3.time.StopWatch;

public class RenderingSpeed
{
    public static void main(String[] args)
    throws ValidityException, ClassNotFoundException, NoSuchAlgorithmException, ParsingException, IOException, InvalidBinaryFileException, InterruptedException
    {
        System.out.println("Start main");
        
        int xRes = 2048;
        int minIter = 256;
        int maxIter = 4096;
        long pointsCount = 10_000_000L;
        
        // find an appropriate max depth
        int minPointsCountPerNode = 256;
        int maxDepth = 0;
        while (pointsCount / Math.pow(4, maxDepth) >= minPointsCountPerNode)
        {
            maxDepth++;
        }
        maxDepth--;
        System.out.println("Max depth : " + maxDepth);
        
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "bck",
        "p256i65536d5D" + maxDepth + "binNoIndex"), new ConsoleQuadTreeManagerListener());
        
        Rectangle viewport = new Rectangle(new Point(2.0, 2.0), new Point(-2.0, -2.0));
        
        StatusQuadTreeNode root = manager.getQuadTreeRoot();
        
        root.strip(maxDepth);
        Path quadTreeSavePath = FileSystems.getDefault().getPath("F:", "dev", "nebula", "render", "x" + xRes,
        "p" + pointsCount + "m" + minIter + "M" + maxIter, "quad" + maxDepth);
        testQuadTreeRenderingSpeed(manager.getQuadTreeRoot(), xRes, viewport, pointsCount, minIter, maxIter, quadTreeSavePath);
        System.out.println("Saved to: " + quadTreeSavePath);
        manager.saveToBinaryFile(FileSystems.getDefault().getPath(quadTreeSavePath.toString(), "tree"), false);
        
        System.out.println("End main");
    }
    
    private static RawMandelbrotData testQuadTreeRenderingSpeed(StatusQuadTreeNode quadTreeRoot, int xRes, RectangleInterface viewport, long pointsCount,
    int minIter, int maxIter, Path savePath)
    throws IOException
    {
        System.out.println("quad");
        int yRes = (int) (xRes / viewport.getWidth() * viewport.getHeight());
        
        NebulabrotRenderer renderer = new NebulabrotRenderer(xRes, yRes, viewport);
        renderer.addMandelbrotRendererListener(new ConsoleMandelbrotRendererListener());
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        RawMandelbrotData rawMandelbrotData = renderer.quadTreeRender(pointsCount, minIter, maxIter, quadTreeRoot, 1);
        stopWatch.stop();
        
        rawMandelbrotData.addAdditionnalInformation("rendering.method", "quadTree");
        rawMandelbrotData.addAdditionnalInformation("rendering.time", Double.toString(stopWatch.getNanoTime() / 1_000_000_000.0d));
        BufferedImage image = rawMandelbrotData.computeBufferedImage(new GrayScaleColorModel(), 3);
        rawMandelbrotData.save(savePath);
        ImageIO.write(image, "PNG", new File(savePath.toFile(), "preview.png"));
        
        return rawMandelbrotData;
    }
    
    private static RawMandelbrotData testLinearRenderingSpeed(int xRes, RectangleInterface viewport, long pointsCount, int minIter, int maxIter, Path savePath)
    throws IOException
    {
        System.out.println("lin");
        int yRes = (int) (xRes / viewport.getWidth() * viewport.getHeight());
        
        NebulabrotRenderer renderer = new NebulabrotRenderer(xRes, yRes, viewport);
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
