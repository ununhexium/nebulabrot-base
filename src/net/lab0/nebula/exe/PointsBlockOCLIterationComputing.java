package net.lab0.nebula.exe;

import java.nio.IntBuffer;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.OpenCLManager;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.SimpleJob;

/**
 * Computes the iterations of a {@link PointsBlock} using GPU computing power.
 * 
 * @author 116
 * 
 */
public class PointsBlockOCLIterationComputing
extends SimpleJob<PointsBlock, PointsBlock>
{
    private long                maximumIteration;
    
    public PointsBlockOCLIterationComputing(CascadingJob<?, PointsBlock> parent, JobBuilder<PointsBlock> jobBuilder,
    PointsBlock pointsBlock, long maximumIteration)
    {
        super(parent, jobBuilder, pointsBlock);
        this.maximumIteration = maximumIteration;
    }
    
    @Override
    public PointsBlock singleStep(PointsBlock input)
    {
        IntBuffer result = OpenCLManager.getInstance().compute(input.real, input.imag, maximumIteration);
        result.rewind();
        for (int i = 0; i < result.capacity(); ++i)
        {
            input.iter[i] = result.get();
        }
        
        return input;
    }
    
}
