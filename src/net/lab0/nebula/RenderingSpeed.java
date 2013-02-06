package net.lab0.nebula;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.lab0.nebula.core.NebulabrotRenderer;
import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.RawMandelbrotData;
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
        
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "bck", "p256i65536d5D16binNoIndex"),
        new ConsoleQuadTreeManagerListener());
        
//         QuadTreeManager manager = new QuadTreeManager(new QuadTreeNode(-2.0, 2.0, -2.0, 2.0), 256, 65536, 5, 0);
//         manager.addQuadTreeManagerListener(new ConsoleQuadTreeManagerListener());
//         manager.addQuadTreeComputeListener(new ConsoleQuadTreeComputeListener());
//         manager.setThreads(Runtime.getRuntime().availableProcessors());
        
        List<Integer> quadTreeMaxDepthList = Arrays.asList(15);
        
        int xRes = 8192*2;
        int minIter = 256;
        int maxIter = 65539;
        long pointsCount = 100_000_000_000L;
        
        List<RectangleInterface> viewports = new ArrayList<>();
        viewports.add(new Rectangle(new Point(2.0, 2.0), new Point(-2.0, -2.0)));
        
        QuadTreeNode root = manager.getQuadTreeRoot();
        
        for (RectangleInterface viewport : viewports)
        {
//            Path linSavePath = FileSystems.getDefault().getPath("F:", "dev", "nebula", "render", "x" + xRes, "p" + pointsCount + "m" + minIter + "M" + maxIter,
//            "lin");
//            RawMandelbrotData linRaw = testLinearRenderingSpeed(xRes, viewports.get(0), pointsCount, minIter, maxIter, linSavePath);
            
            for (Integer quadTreeMaxDepth : quadTreeMaxDepthList)
            {
                root.strip(quadTreeMaxDepth);
                Path quadTreeSavePath = FileSystems.getDefault().getPath("F:", "dev", "nebula", "render", "x" + xRes,
                "p" + pointsCount + "m" + minIter + "M" + maxIter, "quad" + quadTreeMaxDepth);
                testQuadTreeRenderingSpeed(manager.getQuadTreeRoot(), xRes, viewport, pointsCount, minIter, maxIter, quadTreeSavePath);
                manager.saveToXML(FileSystems.getDefault().getPath(quadTreeSavePath.toString(), "tree"));
            }
        }
        
        System.out.println("End main");
    }
    
    private static RawMandelbrotData testQuadTreeRenderingSpeed(QuadTreeNode quadTreeRoot, int xRes, RectangleInterface viewport, long pointsCount,
    int minIter, int maxIter, Path savePath)
    throws IOException
    {
        System.out.println("quad");
        int yRes = (int) (xRes / viewport.getWidth() * viewport.getHeight());
        
        NebulabrotRenderer renderer = new NebulabrotRenderer(xRes, yRes, viewport);
         renderer.addMandelbrotRendererListener(new ConsoleMandelbrotRendererListener());
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        RawMandelbrotData rawMandelbrotData = renderer.quadTreeRender(pointsCount, minIter, maxIter, quadTreeRoot);
        stopWatch.stop();
        
        rawMandelbrotData.addAdditionnalInformation("rendering.method", "quadTree");
        rawMandelbrotData.addAdditionnalInformation("rendering.time", Double.toString(stopWatch.getNanoTime() / 1_000_000_000.0d));
        rawMandelbrotData.save(savePath, true);
        
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
        RawMandelbrotData rawMandelbrotData = renderer.linearRender(pointsCount, minIter, maxIter);
        stopWatch.stop();
        
        rawMandelbrotData.addAdditionnalInformation("rendering.method", "linear");
        rawMandelbrotData.addAdditionnalInformation("rendering.time", Double.toString(stopWatch.getNanoTime() / 1_000_000_000.0d));
        rawMandelbrotData.save(savePath, true);
        
        return rawMandelbrotData;
    }
}
