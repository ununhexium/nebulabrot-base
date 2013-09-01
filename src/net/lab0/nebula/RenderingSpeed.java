package net.lab0.nebula;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.lab0.nebula.color.GrayScaleColorModel;
import net.lab0.nebula.core.NebulabrotRenderer;
import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.listener.ConsoleMandelbrotRendererListener;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import net.lab0.tools.Pair;
import net.lab0.tools.PowerOfTen;
import net.lab0.tools.PowerOfTwoInteger;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import net.lab0.tools.geom.RectangleInterface;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.lang3.time.StopWatch;

public class RenderingSpeed
{
    public enum Parameter
    {
        minPointsPerNode, // must be set first because this parameter may require a reload of the quad tree
        resolution,
        minIter,
        maxIter,
        points,
    }
    
    public static void main(String[] args)
    throws ValidityException, ClassNotFoundException, NoSuchAlgorithmException, ParsingException, IOException, InvalidBinaryFileException, InterruptedException
    {
        System.out.println("Start main");
        
        int timeout = 60 * 1000; // the timeout time for each test, in MS
        
        // the parameters to modify
        // parameters: <"name", <default value, range>>
        Map<Parameter, List<? extends Number>> parameters = new HashMap<>();
        int powerOfTwoElementsCount = PowerOfTwoInteger.values().length;
        List<Integer> resolutionValues = new ArrayList<>(powerOfTwoElementsCount);
        List<Integer> minIterValues = new ArrayList<>(powerOfTwoElementsCount);
        List<Integer> maxIterValues = new ArrayList<>(powerOfTwoElementsCount);
        List<Integer> minPointsCountPerNodeValues = new ArrayList<>();
        
        int powerOfTenCount = PowerOfTen.values().length;
        
        List<Long> pointsCountValues = new ArrayList<>(powerOfTenCount);
        
        // values init
        for (PowerOfTwoInteger po2 : PowerOfTwoInteger.values())
        {
            if (po2.value >= 64 && po2.value <= 17000)
            {
                resolutionValues.add(po2.value);
            }
            minIterValues.add(po2.value);
            maxIterValues.add(po2.value);
        }
        
        for (PowerOfTen po10 : PowerOfTen.values())
        {
            pointsCountValues.add(po10.value);
        }
        
        for (int i = 65536; i >= 1; i /= 2)
        {
            minPointsCountPerNodeValues.add(i);
        }
        
//        parameters.put(Parameter.resolution, resolutionValues);
//        parameters.put(Parameter.minIter, minIterValues);
//        parameters.put(Parameter.maxIter, maxIterValues);
        parameters.put(Parameter.minPointsPerNode, minPointsCountPerNodeValues);
//        parameters.put(Parameter.points, pointsCountValues);
        
        // the default values when only 1 parameter changes
        int defaultResolution = 512;
        int defaultMinIter = 512;
        int defaultMaxIter = 1024;
        int defaultMinPointsCountPerNode = 64;
        long defaultPointsCount = defaultResolution * defaultResolution * defaultMinIter;
        Rectangle defaultViewport = new Rectangle(new Point(-0.5, -0.6), new Point(0, -1.1));
        
        // load the best quadtree
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault()
        .getPath("F:", "dev", "nebula", "tree", "bck", "p256i65536d5D" + 1 + "binNoIndex"), new ConsoleQuadTreeManagerListener());
        
        for (Parameter parameterToTest : Parameter.values())
        {
            try (
                PrintWriter pw = new PrintWriter(FileSystems.getDefault().getPath("TestResults_" + parameterToTest + ".txt").toFile()))
            {
                // set the default value then replace with the value to test
                int resolution = defaultResolution;
                int minIter = defaultMinIter;
                int maxIter = defaultMaxIter;
                int minPointsCountPerNode = defaultMinPointsCountPerNode;
                long pointsCount = defaultPointsCount;
                Rectangle viewport = defaultViewport;
                
                pw.println("Testing " + parameterToTest + " with:");
                pw.println("resolution=" + resolution);
                pw.println("minIter=" + minIter);
                pw.println("maxIter=" + maxIter);
                pw.println("minPointsCountPerNode=" + minPointsCountPerNode);
                pw.println("pointsCount=" + pointsCount);
                pw.println("viewport=" + viewport);
                pw.println("Time;Value");
                
                List<? extends Number> values = parameters.get(parameterToTest);
                
                for (Number n : values)
                {
                    // get the value to test in the appropriate field
                    switch (parameterToTest)
                    {
                        case maxIter:
                            maxIter = n.intValue();
                            break;
                        
                        case minIter:
                            minIter = n.intValue();
                            break;
                        
                        case minPointsPerNode:
                            minPointsCountPerNode = n.intValue();
                            break;
                        
                        case points:
                            pointsCount = n.longValue();
                            break;
                        
                        case resolution:
                            resolution = n.intValue();
                            break;
                    }
                    
                    if (minIter >= maxIter)
                    {
                        continue;
                    }
                    
                    // find an appropriate max depth
                    int maxDepth = getMaxDepth(pointsCount, minPointsCountPerNode);
                    System.out.println("Max depth : " + maxDepth);
                    if (maxDepth > 16)
                    {
                        maxDepth = 16;
                    }
                    
                    if (manager.getQuadTreeRoot().getMaxNodeDepth() < maxDepth)
                    {
                        // load the appropriate quadtree
                        manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "bck",
                        "p256i65536d5D" + maxDepth + "binNoIndex"), new ConsoleQuadTreeManagerListener());
                        StatusQuadTreeNode root = manager.getQuadTreeRoot();
                        root.strip(maxDepth);
                    }
                    
                    // the actual test
                    final Path quadTreeSavePath = FileSystems.getDefault().getPath("F:", "dev", "nebula", "render", "x" + resolution,
                    "p" + pointsCount + "m" + minIter + "M" + maxIter, "quad" + maxDepth);
                    
                    TestingThread testingThread = new TestingThread(manager.getQuadTreeRoot(), resolution, viewport, pointsCount, minIter, maxIter,
                    quadTreeSavePath);
                    
                    testingThread.start();
                    
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    boolean goOn = true;
                    while (goOn)
                    {
                        stopWatch.split();
                        // if we timeout -> stop the test
                        if (stopWatch.getSplitTime() > timeout)
                        {
                            pw.println("Timed out, waiting for the thread to join");
                            testingThread.stopTest();
                            testingThread.join();
                        }
                        
                        // if the test is over -> exit this loop
                        if (!testingThread.isAlive())
                        {
                            RawMandelbrotData data = testingThread.getData();
                            pw.println("" + data.getAdditionnalInformation("rendering.time") + ";" + n);
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("End main");
    }
    
    private static int getMaxDepth(long pointsCount, int minPointsCountPerNode)
    {
        int maxDepth = 0;
        while (pointsCount / Math.pow(4, maxDepth) >= minPointsCountPerNode)
        {
            maxDepth++;
        }
        maxDepth--;
        return maxDepth;
    }
}
