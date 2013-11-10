package net.lab0.nebula.exe.builder;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.PointsBlockCPUIterationComputing;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;

/**
 * This class takes a {@link PointsBlock} and creates a {@link PointsBlockCPUIterationComputing} that will process it.
 * 
 * @author 116
 * 
 */
public class ToCPUIterationComputating
implements JobBuilder<PointsBlock>
{
    private final JobBuilder<PointsBlock> jobBuilder;
    
    /**
     * 
     * @param jobBuilder
     *            the next job builder that will be used at the {@link PointsBlockCPUIterationComputing} creation time.
     */
    public ToCPUIterationComputating(JobBuilder<PointsBlock> jobBuilder)
    {
        super();
        this.jobBuilder = jobBuilder;
    }
    
    @Override
    public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
    {
        return new PointsBlockCPUIterationComputing(parent.getExecutor(), parent.getPriority() + 1, jobBuilder, output,
        65536);
    }
    
}
