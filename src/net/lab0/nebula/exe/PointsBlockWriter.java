package net.lab0.nebula.exe;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;

public class PointsBlockWriter
extends CascadingJob<PointsBlock, Void>
{
    public PointsBlockWriter(ExecutorService executor, int priority, JobBuilder<Void> jobBuilder, Path ouputPath)
    {
        super(executor, priority, jobBuilder);
    }
    
    @Override
    public void executeTask()
    {
        
    }
    
}
