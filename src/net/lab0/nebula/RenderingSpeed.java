package net.lab0.nebula;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
    throws ValidityException, ClassNotFoundException, NoSuchAlgorithmException, ParsingException, IOException, InvalidBinaryFileException
    {
        System.out.println("Start main");
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "p256i65536d5D16binaryNoIndex"),
        new ConsoleQuadTreeManagerListener());
        
        List<RectangleInterface> viewPorts = new ArrayList<>();
        viewPorts.add(new Rectangle(new Point(2.0, 2.0), new Point(-2.0, -2.0)));
        
        int xRes = 256;
        int minIter = 0;
        int maxIter = 256;
        long pointsCount = xRes * xRes * maxIter;
        
        testLinearRenderingSpeed(xRes, viewPorts.get(0), pointsCount, minIter, maxIter);
        testQuadTreeRenderingSpeed(manager.getQuadTreeRoot(), xRes, viewPorts.get(0), pointsCount, minIter, maxIter);
        
        System.out.println("End main");
    }
    
    private static void testQuadTreeRenderingSpeed(QuadTreeNode quadTreeRoot, int xRes, RectangleInterface viewport, long pointsCount, int minIter, int maxIter)
    throws IOException
    {
        int yRes = (int) (xRes / viewport.getWidth() * viewport.getHeight());
        
        NebulabrotRenderer renderer = new NebulabrotRenderer(xRes, yRes, viewport);
        renderer.addMandelbrotRendererListener(new ConsoleMandelbrotRendererListener());
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        RawMandelbrotData rawMandelbrotData = renderer.quadTreeRender(pointsCount, minIter, maxIter, quadTreeRoot);
        stopWatch.stop();
        
        rawMandelbrotData.addAdditionnalInformation("rendering.method", "quadTree");
        rawMandelbrotData.addAdditionnalInformation("rendering.time", Double.toString(stopWatch.getNanoTime() / 1_000_000_000.0d));
        rawMandelbrotData.save(
        FileSystems.getDefault().getPath("F:", "dev", "nebula", "render", "quad" + "x" + xRes + "p" + pointsCount + "m" + minIter + "M" + maxIter), true);
    }
    
    private static void testLinearRenderingSpeed(int xRes, RectangleInterface viewport, long pointsCount, int minIter, int maxIter)
    throws IOException
    {
        int yRes = (int) (xRes / viewport.getWidth() * viewport.getHeight());
        
        NebulabrotRenderer renderer = new NebulabrotRenderer(xRes, yRes, viewport);
        renderer.addMandelbrotRendererListener(new ConsoleMandelbrotRendererListener());
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        RawMandelbrotData rawMandelbrotData = renderer.linearRender(pointsCount, minIter, maxIter);
        stopWatch.stop();
        
        rawMandelbrotData.addAdditionnalInformation("rendering.method", "linear");
        rawMandelbrotData.addAdditionnalInformation("rendering.time", Double.toString(stopWatch.getNanoTime() / 1_000_000_000.0d));
        rawMandelbrotData.save(
        FileSystems.getDefault().getPath("F:", "dev", "nebula", "render", "lin" + "x" + xRes + "p" + pointsCount + "m" + minIter + "M" + maxIter), true);
    }
}
