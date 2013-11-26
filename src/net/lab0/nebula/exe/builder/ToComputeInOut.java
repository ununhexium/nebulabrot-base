package net.lab0.nebula.exe.builder;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.exe.ComputeInOutForQuadTreeNode;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;

public class ToComputeInOut
implements JobBuilder<MandelbrotQuadTreeNode>
{
    private JobBuilder<MandelbrotQuadTreeNode> jobBuilder;
    private long                               maximumIteration;
    private int                                sidePointsCount;
    private long                               iterationDifferenceLimit;
    
    public ToComputeInOut(JobBuilder<MandelbrotQuadTreeNode> jobBuilder, long maximumIteration, int sidePointsCount,
    long iterationDifferenceLimit)
    {
        super();
        this.jobBuilder = jobBuilder;
        this.maximumIteration = maximumIteration;
        this.sidePointsCount = sidePointsCount;
        this.iterationDifferenceLimit = iterationDifferenceLimit;
    }
    
    @Override
    public CascadingJob<MandelbrotQuadTreeNode, ?> buildJob(CascadingJob<?, MandelbrotQuadTreeNode> parent,
    MandelbrotQuadTreeNode output)
    {
        return new ComputeInOutForQuadTreeNode(parent.getExecutor(), parent.getPriority() + 1, jobBuilder, output,
        maximumIteration, sidePointsCount, iterationDifferenceLimit);
    }
    
}
