
package net.lab0.nebula;


import java.io.IOException;
import java.nio.file.FileSystems;

import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import nu.xom.ParsingException;
import nu.xom.ValidityException;


public class Main
{
    private static QuadTreeNode root;
    
    public static void main(String[] args) throws IOException, ValidityException, ParsingException
    {
        System.out.println("Start");
        
// loopCompute();
// f2();
// createSaveLoadSave();
        
        root = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
        
        int maxDepth = 9;
        int pointsPerSide = 100;
        int maxIter = 512;
        int diffIterLimit = 5;
// QuadTreeManager manager = new QuadTreeManager(root, pointsPerSide, maxIter, diffIterLimit, maxDepth);
// manager.compute(Long.MAX_VALUE);
// manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "p" + pointsPerSide + "i" + maxIter + "d" + diffIterLimit + "D" + maxDepth), true,
// maxDepth + 2);
        
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath(".", "out",
        "p" + pointsPerSide + "i" + maxIter + "d" + diffIterLimit + "D" + maxDepth));
        
        MandelbrotRenderer mandelbrotRenderer = new MandelbrotRenderer(512, 512, new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0)));
        RawMandelbrotData raw = mandelbrotRenderer.linearRender(2<<22, 100, 200);
        
        new DisplayFrame(raw);
        
        System.out.println("End");
    }
    
// private static void createSaveLoadSave() throws IOException, ValidityException, ParsingException
// {
// root = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
//
// QuadTreeManager manager = new QuadTreeManager(root, 100, 512, 5, 13);
// manager.setMaxDepth(8);
// manager.compute(Long.MAX_VALUE);
// manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "testStart"), true, 10);
//
// QuadTreeManager manager2 = new QuadTreeManager(FileSystems.getDefault().getPath(".", "out", "testStart"));
// Statistics statistics = manager2.computeStatistics();
// // System.out.println(statistics);
//
// manager2.saveToXML(FileSystems.getDefault().getPath(".", "out", "testEnd"), true, 10);
// }
//
// private static void f2() throws ValidityException, ParsingException, IOException
// {
// QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath(".", "out", "outN10000.xml"));
// System.out.println("Reading done");
// manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "test"), true, 6);
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
// manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "N" + ((i + 1) * step)), false, 0);
// System.out.println("Computing time = " + manager.getTotalComputingTime());
// }
// }
}
