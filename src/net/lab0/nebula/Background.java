package net.lab0.nebula;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.Statistics;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class Background
{
    public static void main(String[] args) throws InterruptedException, IOException, ValidityException, ParsingException
    {
        QuadTreeNode root = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
        
        int maxDepth = 8;
        int pointsPerSide = 256;
        int maxIter = 4096;
        int diffIterLimit = 5;
        // bruteForceVSQuadTreeComparison(maxDepth, pointsPerSide, maxIter,
        // diffIterLimit);
        
        long startTimer = System.currentTimeMillis();
        // GregorianCalendar cal = new GregorianCalendar(2012, Calendar.AUGUST, 5, 21, 35);
        // Date stop = cal.getTime();
        
        int threads = Runtime.getRuntime().availableProcessors();
        System.out.println("Using " + threads + " threads");
        // QuadTreeManager manager = new QuadTreeManager(root, pointsPerSide, maxIter, diffIterLimit, maxDepth, threads / 2);
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "p256i65536d5D16v123"));
        manager.setThreads(threads);
        
        // System.out.println("start " + new Date());
        // System.out.println("end after " + stop);
        
        long computedNodes = 0;
        long nodesPerCycle = 100_000;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, 20);
        int pass = 123;
        
        boolean goOn = true;
        while (goOn)
        // while (computedNodes < nodesPerCycle*10)
        {
             pass++;
            goOn = manager.compute(nodesPerCycle);
            computedNodes += nodesPerCycle;
            System.out.println("Nodes computed so far : " + computedNodes);
            
//            Statistics statistics = manager.computeStatistics();
//            System.out.println(statistics);
            
            Path path = FileSystems.getDefault()
                    .getPath(
                            "F:",
                            "dev",
                            "nebula",
                            "tree",
                            "p" + manager.getPointsPerSide() + "i" + manager.getMaxIter() + "d" + manager.getDiffIterLimit() + "D" + manager.getMaxDepth()
                                    + "v" + pass);
            
            System.out.println("save to " + path);
            manager.saveToXML(path, true, 6);
        }
        
        long endTimer = System.currentTimeMillis();
        
        // Statistics statistics = manager.computeStatistics();
        // System.out.println(statistics);
        
        // System.out.println("Searched for " + manager.getSearchTime() + "ms and " + manager.getSearchCounter() + " times");
        System.out.println("Computing time = " + (endTimer - startTimer));
        
        // QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("M:\\p256i65536d5D12v5"));
        
        // QuadTreeManager manager = new QuadTreeManager(root, pointsPerSide, maxIter, 5, 5, 6);
        // manager.compute(1000000);
        // manager.saveToXML(FileSystems.getDefault().getPath("M:\\p256i65536d5D12v6"), true, 3);
        
        System.out.println("End");
    }
}
