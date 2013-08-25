package net.lab0.nebula;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.data.RootQuadTreeNode;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class Background
{
    public static void main(String[] args)
    throws InterruptedException, IOException, ValidityException, ParsingException, ClassNotFoundException, InvalidBinaryFileException, NoSuchAlgorithmException
    {
        StatusQuadTreeNode root = new RootQuadTreeNode(-2.0, 2.0, -2.0, 2.0);
        
        int maxDepth = 8;
        int pointsPerSide = 256;
        int maxIter = 4096;
        int diffIterLimit = 5;
        
        long startTimer = System.currentTimeMillis();
        // GregorianCalendar cal = new GregorianCalendar(2012, Calendar.AUGUST, 5, 21, 35);
        // Date stop = cal.getTime();
        
        int threads = Runtime.getRuntime().availableProcessors() - 1;
        System.out.println("Using " + threads + " threads");
        // QuadTreeManager manager = new QuadTreeManager(root, pointsPerSide, maxIter, diffIterLimit, maxDepth, threads / 2);
        
        int pass = 430;
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "p256i65536d5D16v" + pass),
        new ConsoleQuadTreeManagerListener());
        manager.setThreads(threads);
        
        // System.out.println("start " + new Date());
        // System.out.println("end after " + stop);
        
        long computedNodes = 0;
        int nodesPerCycle = 100_000;
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 90);

        GregorianCalendar end = new GregorianCalendar();
        end.setTime(new Date());
        end.add(Calendar.DAY_OF_YEAR, 2);
        
        boolean goOn = true;
        // while (goOn)
        while (new Date().before(end.getTime()))
        {
            goOn = manager.compute(nodesPerCycle);
            computedNodes += (long) nodesPerCycle;
            System.out.println("Nodes computed so far : " + computedNodes);
            
            // Statistics statistics = manager.computeStatistics();
            // System.out.println(statistics);
            
            if (new Date().after(cal.getTime())) //save every 30 minutes
            {
                pass++;
                Path path = FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree",
                "p" + manager.getPointsPerSide() + "i" + manager.getMaxIter() + "d" + manager.getDiffIterLimit() + "D" + manager.getMaxDepth() + "v" + pass);
                
                System.out.println("save to " + path);
                manager.saveToBinaryFile(path, false);

                cal.add(Calendar.MINUTE, 90);
            }
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
