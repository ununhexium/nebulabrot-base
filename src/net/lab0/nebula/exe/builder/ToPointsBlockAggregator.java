package net.lab0.nebula.exe.builder;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exe.NebulaGatherer;
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
    
    /**
     * 
     * @param aggregate
     * @param viewPort
     * @param minimumIteration
     * @param maximumIteration
     */
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
        return new NebulaGatherer(parent, output, aggregate, viewPort,
        minimumIteration, maximumIteration);
    }
}
