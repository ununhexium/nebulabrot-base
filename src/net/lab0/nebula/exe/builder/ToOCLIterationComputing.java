package net.lab0.nebula.exe.builder;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.PointsBlockCPUIterationComputing;
import net.lab0.nebula.exe.PointsBlockOCLIterationComputing;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;

/**
 * This class takes a {@link PointsBlock} and creates a {@link PointsBlockOCLIterationComputing} that will process it.
 * 
 * @author 116
 * 
 */
public class ToOCLIterationComputing
implements JobBuilder<PointsBlock>
{
    private final JobBuilder<PointsBlock> jobBuilder;
    private final long                    iteration;
    
    /**
     * 
     * @param jobBuilder
     *            the next job builder that will be used at the {@link PointsBlockCPUIterationComputing} creation time.
     */
    public ToOCLIterationComputing(JobBuilder<PointsBlock> jobBuilder, long iteration)
    {
        super();
        this.jobBuilder = jobBuilder;
        this.iteration = iteration;
    }
    
    @Override
    public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
    {
        return new PointsBlockOCLIterationComputing(parent, jobBuilder, output,
        iteration);
    }
    
}
