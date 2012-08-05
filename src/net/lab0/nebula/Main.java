
package net.lab0.nebula;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Date;

import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.SynchronizedCounter;
import nu.xom.ParsingException;
import nu.xom.ValidityException;


public class Main
{
    private static QuadTreeNode root;
    
    public static void main(String[] args) throws IOException, ValidityException, ParsingException, InterruptedException
    {
        System.out.println("Start");
        
        // loopCompute();
        // f2();
        // createSaveLoadSave();
        
        root = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
        
        int maxDepth = 12;
        int pointsPerSide = 256;
        int maxIter = 65536;
        int diffIterLimit = 5;
        // bruteForceVSQuadTreeComparison(maxDepth, pointsPerSide, maxIter,
        // diffIterLimit);
        
        // long startTimer = System.currentTimeMillis();
        // Date now = new Date();
        // Date stop = new Date(now.getTime() + 8 * 3600 * 1000);
        //
        // QuadTreeManager manager = new QuadTreeManager(root, pointsPerSide, maxIter, diffIterLimit, maxDepth, 5);
        //
        // System.out.println("start " + now);
        // System.out.println("end after " + stop);
        //
        // int version = 0;
        // while (new Date().before(stop))
        // {
        // version++;
        // manager.compute(new SynchronizedCounter(100000));
        // manager.saveToXML(
        // FileSystems.getDefault().getPath(".", "out", "p" + pointsPerSide + "i" + maxIter + "d" + diffIterLimit + "D" + maxDepth + "v" + version), true, 6);
        //
        // Statistics statistics = manager.computeStatistics();
        // System.out.println(statistics);
        // }
        //
        // long endTimer = System.currentTimeMillis();
        //
        // Statistics statistics = manager.computeStatistics();
        // System.out.println(statistics);
        //
        // System.out.println("Computing time = " + (endTimer - startTimer));
        
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath(".", "out",
        "p" + pointsPerSide + "i" + maxIter + "d" + diffIterLimit + "D" + maxDepth + "v3"));
        
        manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "p" + pointsPerSide + "i" + maxIter + "d" + diffIterLimit + "D" + maxDepth + "v4"), true, 100);
        
        System.out.println("End");
    }
    // private static void bruteForceVSQuadTreeComparison(int maxDepth, int pointsPerSide, int maxIter, int diffIterLimit) throws ValidityException,
    // ParsingException, IOException
    // {
    // // QuadTreeManager manager = new QuadTreeManager(root, pointsPerSide,
    // // maxIter, diffIterLimit, maxDepth);
    // // manager.compute(Long.MAX_VALUE);
    // // manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "p" +
    // // pointsPerSide + "i" + maxIter + "d" + diffIterLimit + "D" +
    // // maxDepth), true,
    // // maxDepth + 2);
    //
    // QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath(".", "out",
    // "p" + pointsPerSide + "i" + maxIter + "d" + diffIterLimit + "D" + maxDepth));
    //
    // if (manager.getQuadTreeRoot().getNodeByPath("R").hasComputedChildren())
    // {
    // System.out.println("ok");
    // }
    //
    // MandelbrotRenderer mandelbrotRenderer = new MandelbrotRenderer(1000, 1000, new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0)));
    //
    // long startTimer = System.currentTimeMillis();
    // RawMandelbrotData raw = mandelbrotRenderer.linearRender(20 * 1000 * 1000, 100, 200);
    // long endTimer = System.currentTimeMillis();
    // new DisplayFrame("Brute force in " + (endTimer - startTimer) + " ms", raw);
    //
    // startTimer = System.currentTimeMillis();
    // RawMandelbrotData raw2 = mandelbrotRenderer.quadTreeRender(2L << 26L, 100, 200, manager.getQuadTreeRoot());
    // endTimer = System.currentTimeMillis();
    // new DisplayFrame("Quad Tree in " + (endTimer - startTimer) + " ms", raw2);
    // }
    
    // private static void createSaveLoadSave() throws IOException,
    // ValidityException, ParsingException
    // {
    // root = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
    //
    // QuadTreeManager manager = new QuadTreeManager(root, 100, 512, 5, 13);
    // manager.setMaxDepth(8);
    // manager.compute(Long.MAX_VALUE);
    // manager.saveToXML(FileSystems.getDefault().getPath(".", "out",
    // "testStart"), true, 10);
    //
    // QuadTreeManager manager2 = new
    // QuadTreeManager(FileSystems.getDefault().getPath(".", "out",
    // "testStart"));
    // Statistics statistics = manager2.computeStatistics();
    // // System.out.println(statistics);
    //
    // manager2.saveToXML(FileSystems.getDefault().getPath(".", "out",
    // "testEnd"), true, 10);
    // }
    //
    // private static void f2() throws ValidityException, ParsingException,
    // IOException
    // {
    // QuadTreeManager manager = new
    // QuadTreeManager(FileSystems.getDefault().getPath(".", "out",
    // "outN10000.xml"));
    // System.out.println("Reading done");
    // manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "test"),
    // true, 6);
    // Statistics statistics = manager.computeStatistics();
    // System.out.println(statistics);
    // }
    //
    // private static void loopCompute() throws IOException
    // {
    // root = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
    //
    // QuadTreeManager manager = new QuadTreeManager(root, 100, 512, 5, 13);
    // long step = 5000;
    // for (long i = 0; i < 200; ++i)
    // {
    // manager.compute(step);
    // manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "N" + ((i
    // + 1) * step)), false, 0);
    // System.out.println("Computing time = " +
    // manager.getTotalComputingTime());
    // }
    // }
}
