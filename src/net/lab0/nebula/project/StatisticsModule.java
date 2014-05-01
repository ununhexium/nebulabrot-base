package net.lab0.nebula.project;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import net.lab0.nebula.exe.MandelbrotQTNStats;
import net.lab0.nebula.exe.MandelbrotQTNStats.Aggregate;
import net.lab0.nebula.exe.MandelbrotQuadTreeNodeReader;
import net.lab0.nebula.exe.builder.BuilderFactory;
import net.lab0.tools.exec.PriorityExecutor;

public class StatisticsModule
extends Module
{
    
    public StatisticsModule(Project project)
    {
        super(project);
    }
    
    public Map<Integer, Aggregate> computeTreeStatistics(int id)
    {
        Path p = project.getQuadTreeFolderPath(id);
        int blockSize = 1024 * 1024;
        
        // the results <depth, count>
        Map<Integer, Aggregate> results = new HashMap<>();
        
        // compute the stats for each depth
        for (int i = 0; i < Integer.MAX_VALUE; ++i)
        {
            Path current = p.resolve("d_" + i + ".data");
            try
            {
                if (!current.toFile().exists())
                {
                    break;
                }
                
                PriorityExecutor priorityExecutor = new PriorityExecutor(Runtime.getRuntime().availableProcessors() - 1);
                MandelbrotQTNStats.Aggregate aggregate = new Aggregate();
                MandelbrotQuadTreeNodeReader reader = new MandelbrotQuadTreeNodeReader(priorityExecutor,
                BuilderFactory.toMandelbrotQTNStats(aggregate), current, blockSize);
                priorityExecutor.execute(reader);
                priorityExecutor.waitForFinish();
                
                results.put(i, aggregate);
            }
            catch (FileNotFoundException | InterruptedException e)
            {
                throw new RuntimeException("Error while reading the file " + current);
            }
        }
        
        return results;
    }
}
