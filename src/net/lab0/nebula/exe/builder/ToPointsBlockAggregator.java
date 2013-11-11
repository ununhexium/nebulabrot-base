package net.lab0.nebula.exe.builder;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exe.NebulaAggregator;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.geom.RectangleInterface;

public class ToPointsBlockAggregator
implements JobBuilder<PointsBlock>
{
    private RawMandelbrotData  aggregate;
    private RectangleInterface viewPort;
    private long               maximumIteration;
    private long               minimumIteration;
    
    public ToPointsBlockAggregator(RawMandelbrotData aggregate, RectangleInterface viewPort, long minimumIteration, long maximumIteration)
    {
        super();
        this.aggregate = aggregate;
        this.viewPort = viewPort;
        this.maximumIteration = maximumIteration;
        this.minimumIteration = minimumIteration;
    }
    
    @Override
    public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
    {
        return new NebulaAggregator(parent.getExecutor(), parent.getPriority() + 1, output, aggregate, viewPort,
        minimumIteration, maximumIteration);
    }
}
