package net.lab0.nebula.exe;

import net.lab0.nebula.core.MandelbrotComputeRoutines;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.SimpleJob;

public class PointsBlockCPUIterationComputing
extends SimpleJob<PointsBlock, PointsBlock>
{
    private long maximumIteration;
    
    public PointsBlockCPUIterationComputing(CascadingJob<?, PointsBlock> parent, JobBuilder<PointsBlock> jobBuilder,
    PointsBlock pointsBlock, long maximumIteration)
    {
        super(parent, jobBuilder, pointsBlock);
        this.maximumIteration = maximumIteration;
    }
    
    @Override
    public PointsBlock singleStep(PointsBlock input)
    {
        double[] real = input.real;
        double[] imag = input.imag;
        long[] iter = input.iter;
        for (int i = 0; i < input.size; ++i)
        {
            iter[i] = MandelbrotComputeRoutines.computeIterationsCountOptim2(real[i], imag[i], maximumIteration);
        }
        return input;
    }
    
}
