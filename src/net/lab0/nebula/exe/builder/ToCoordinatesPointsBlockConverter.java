package net.lab0.nebula.exe.builder;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.CoordinatesToPointsBlockConverter;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;

public class ToCoordinatesPointsBlockConverter
implements JobBuilder<CoordinatesBlock>
{
    private JobBuilder<PointsBlock> jobBuilder;
    private int                     pointsBlockSize;
    
    public ToCoordinatesPointsBlockConverter(JobBuilder<PointsBlock> jobBuilder, int pointsBlockSize)
    {
        super();
        this.jobBuilder = jobBuilder;
        this.pointsBlockSize = pointsBlockSize;
    }
    
    @Override
    public CascadingJob<CoordinatesBlock, ?> buildJob(CascadingJob<?, CoordinatesBlock> parent, CoordinatesBlock output)
    {
        return new CoordinatesToPointsBlockConverter(parent, jobBuilder, output, pointsBlockSize);
    }
    
}
