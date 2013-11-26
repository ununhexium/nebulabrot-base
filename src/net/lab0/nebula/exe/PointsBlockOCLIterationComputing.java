package net.lab0.nebula.exe;

import java.nio.IntBuffer;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.OpenCLManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.exec.SimpleJob;

public class PointsBlockOCLIterationComputing
extends SimpleJob<PointsBlock, PointsBlock>
{
    private long                maximumIteration;
    private final OpenCLManager manager;
    
    public PointsBlockOCLIterationComputing(PriorityExecutor executor, int priority,
    JobBuilder<PointsBlock> jobBuilder, PointsBlock pointsBlock, long maximumIteration, OpenCLManager manager)
    {
        super(executor, priority, jobBuilder, pointsBlock);
        this.maximumIteration = maximumIteration;
        this.manager = manager;
    }
    
    @Override
    public PointsBlock singleStep(PointsBlock input)
    {
        IntBuffer result = manager.compute(input.real, input.imag, maximumIteration);
        result.rewind();
        for (int i = 0; i < result.capacity(); ++i)
        {
            input.iter[i] = result.get();
        }
        
        return input;
    }
    
}
