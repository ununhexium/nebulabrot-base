
package net.lab0.nebula;


import java.io.IOException;
import java.nio.file.FileSystems;

import net.lab0.nebula.data.QuadTreeNode;
import nu.xom.ParsingException;
import nu.xom.ValidityException;


public class Main
{
    private static QuadTreeNode root;
    
    public static void main(String[] args) throws IOException, ValidityException, ParsingException
    {
        System.out.println("Start");
        
// loopCompute();
//        f2();

        root = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
        
        QuadTreeManager manager = new QuadTreeManager(root, 100, 512, 5, 13);
        manager.setMaxDepth(6);
        manager.compute(Long.MAX_VALUE);
        manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "testStart"), true, 5);
        
        QuadTreeManager manager2 = new QuadTreeManager(FileSystems.getDefault().getPath(".", "out", "testStart"));
        Statistics statistics = manager2.computeStatistics();
//        System.out.println(statistics);
        
        manager2.saveToXML(FileSystems.getDefault().getPath(".", "out", "testEnd"), true, 5);
        
        System.out.println("End");
    }

    private static void f2() throws ValidityException, ParsingException, IOException
    {
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath(".", "out", "outN10000.xml"));
        System.out.println("Reading done");
        manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "test"), true, 6);
        Statistics statistics = manager.computeStatistics();
        System.out.println(statistics);
    }
    
    private static void loopCompute() throws IOException
    {
        root = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
        
        QuadTreeManager manager = new QuadTreeManager(root, 100, 512, 5, 13);
        long step = 5000;
        for (long i = 0; i < 200; ++i)
        {
            manager.compute(step);
            manager.saveToXML(FileSystems.getDefault().getPath(".", "out", "N" + ((i + 1) * step)), false, 0);
            System.out.println("Computing time = " + manager.getTotalComputingTime());
        }
    }
}
