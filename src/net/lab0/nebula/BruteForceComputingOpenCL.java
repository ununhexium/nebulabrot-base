package net.lab0.nebula;

import java.io.File;
import java.nio.IntBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.lab0.nebula.color.PowerGrayScaleColorModel;
import net.lab0.nebula.core.NebulabrotRenderer;
import net.lab0.nebula.core.OpenClMandelbrotComputeRoutines;
import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import net.lab0.tools.HumanReadable;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;

import org.apache.commons.lang3.time.StopWatch;

public class BruteForceComputingOpenCL
{
    public static void main(String[] args)
    throws Exception
    {
        long size = 1 << 14;
        int blockSize = 1024 * 1024;
        
        System.out.println("Need to compute " + (size * size / (long) blockSize) + " blocks.");
        
        OpenClMandelbrotComputeRoutines ocl = new OpenClMandelbrotComputeRoutines();
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        double step = 4.0 / (double) size;
        double yCurrent = -2.0;
        int index = 0;
        int passes = 0;
        int minIter = 3;
        int maxIter = 4096;
        
        int sub = 0;
        Path path = null;
        do
        {
            path = FileSystems.getDefault().getPath("F:\\dev\\nebula\\raw", "file" + sub);
            sub++;
        } while (path.toFile().exists());
        
        if (!path.toFile().exists())
        {
            path.toFile().mkdirs();
        }
        
        System.out.println("writer");
        XZWriter xzWriter = new XZWriter(path, Runtime.getRuntime().availableProcessors() -1, (long) size * (long) size, minIter, maxIter);
        Thread writerThread = new Thread(xzWriter, "XZ Writer");
        writerThread.start();
        
        System.out.println("mandel");
        passes = bruteForce(size, blockSize, ocl, step, index, passes, maxIter, xzWriter);
//         passes = quadTree(size, blockSize, ocl, step, index, passes, maxIter, minIter, xzWriter);
        
        xzWriter.stopWriter();
        writerThread.join();
        
        System.out.println("Passes: " + passes);
        
        stopWatch.stop();
        System.out.println("OpenCL computation: " + stopWatch.toString() + " , " + xzWriter.getTotalIterations()
        + " iterations.");
        
        long speed = xzWriter.getTotalIterations() / (stopWatch.getTime() / 1000); // iterations per second
        System.out.println(HumanReadable.humanReadableNumber(speed, true) + " CL iteration per second");
        
//        Path path = FileSystems.getDefault().getPath("F:\\dev\\nebula\\raw", "file" + 65);
        
        NebulabrotRenderer nebulabrotRenderer = new NebulabrotRenderer(32768, 32768, new Rectangle(new Point(-2.0, -2.0),
        new Point(2.0, 2.0)));
        File renderFile = new File(path.toFile(), "concat.xz");
        RawMandelbrotData data = nebulabrotRenderer.fileRender(renderFile, 0, Integer.MAX_VALUE);
        data.saveAsTiles(new PowerGrayScaleColorModel(0.5), FileSystems.getDefault().getPath(path.toString() + "_tiles").toFile(),
        512);
    }
    
    private static int quadTree(long size, int blockSize, OpenClMandelbrotComputeRoutines ocl, double step, int index,
    int passes, int maxIter, int minIter, XZWriter xzWriter)
    throws Exception
    {
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree",
        "bck", "p256i65536d5D" + 16 + "binNoIndex"), new ConsoleQuadTreeManagerListener());
        manager.getQuadTreeRoot().strip(8);
        
        final List<StatusQuadTreeNode> nodesList = new ArrayList<>();
        manager.getQuadTreeRoot().getLeafNodes(nodesList, Arrays.asList(Status.BROWSED, Status.OUTSIDE, Status.VOID),
        minIter, maxIter);
        
        System.out.println("Node: " + nodesList.size());
        
        double[] xCoordinates = new double[blockSize];
        double[] yCoordinates = new double[blockSize];
        
        manager = null;
        System.gc();
        int currentNode = 0;
        int percentNode = 0;
        
        for (StatusQuadTreeNode node : nodesList)
        {
            final StatusQuadTreeNode finalNode = node;
            currentNode++;
            if ((currentNode * 100 / nodesList.size()) > percentNode)
            {
                percentNode = (currentNode * 100 / nodesList.size());
                System.out.println("" + percentNode + "% - " + currentNode + " - " + index);
            }
            // System.out.println(++currentNode + " / " + index);
            
            // find the first point inside the node
            
            double xStart = Math.ceil(finalNode.getMinX() / step) * step;
            double yStart = Math.ceil(finalNode.getMinY() / step) * step;
            
            // System.out.println("start1 (" + xStart + ";" + yStart + ")");
            
            double real = xStart;
            double maxX = finalNode.getMaxX();
            double maxY = finalNode.getMaxY();
            
            while (real < maxX)
            {
                double img = yStart;
                while (img < maxY)
                {
                    yCoordinates[index] = real;
                    xCoordinates[index] = img;
                    index++;
                    
                    if (index == blockSize)
                    {
                        System.out.println("Block" + passes);
                        final double[] xCtmp = xCoordinates;
                        final double[] yCtmp = yCoordinates;
                        xCoordinates = new double[blockSize];
                        yCoordinates = new double[blockSize];
                        
                        IntBuffer result = ocl.compute(xCtmp, yCtmp, maxIter);
                        System.out.println("Ended computation");
                        result.rewind();
                        
                        xzWriter.put(result, xCtmp, yCtmp);
                        
                        index = 0;
                        passes++;
                    }
                    
                    img += step;
                }
                
                real += step;
            }
        }
        
        // compute remaining points
        // optim:do not compute these points -> set out of range
        for (int i = index; i < blockSize; ++i)
        {
            xCoordinates[i] = Double.MAX_VALUE;
            yCoordinates[i] = Double.MAX_VALUE;
        }
        
        final double[] xCtmp = xCoordinates;
        final double[] yCtmp = yCoordinates;
        
        IntBuffer result = ocl.compute(xCtmp, yCtmp, maxIter);
        System.out.println("Ended computation");
        result.rewind();
        
        int[] array = new int[index];
        for (int i = 0; i < index; ++i)
        {
            array[i] = result.get();
        }
        
        xzWriter.put(IntBuffer.wrap(array), xCtmp, yCtmp);
        
        return 0;
    }
    
    private static int bruteForce(long size, int blockSize, OpenClMandelbrotComputeRoutines ocl, double step,
    int index, int passes, int maxIter, XZWriter xzWriter)
    {
        double yCurrent;
        double[] xCoordinates = new double[blockSize];
        double[] yCoordinates = new double[blockSize];
        for (long y = 0; y < size; ++y)
        {
            yCurrent = -2.0 + step * y;
            for (long x = 0; x < size; ++x)
            {
                yCoordinates[index] = yCurrent;
                xCoordinates[index] = -2.0 + step * x;
                index++;
                if (index == blockSize)
                {
                    System.out.println("Block" + passes);
                    final double[] xCtmp = xCoordinates;
                    final double[] yCtmp = yCoordinates;
                    xCoordinates = new double[blockSize];
                    yCoordinates = new double[blockSize];
                    
                    IntBuffer result = ocl.compute(xCtmp, yCtmp, maxIter);
                    // System.out.println("Ended computation");
                    result.rewind();
                    
                    xzWriter.put(result, xCtmp, yCtmp);
                    
                    index = 0;
                    passes++;
                }
            }
        }
        return passes;
    }
}
