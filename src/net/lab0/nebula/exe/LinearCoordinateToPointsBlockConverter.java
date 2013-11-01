package net.lab0.nebula.exe;

import java.util.concurrent.ExecutorService;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.PointsBlockManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.MultipleOutputJob;

/**
 * Converts a {@link CoordinatesBlock} into as many {@link PointsBlock} as needed.
 * 
 * @author 116@lab0.net
 * 
 */
public class LinearCoordinateToPointsBlockConverter
extends MultipleOutputJob<CoordinatesBlock, PointsBlock>
{
    private CoordinatesBlock   block;
    private PointsBlock        pointsBlock;
    private final int          pointsBlockSize;
    private double             lastX;
    private double             lastY;
    private PointsBlockManager pointsBlockManager;
    
    /**
     * 
     * @param executor
     *            The executor in which this job has to be executed
     * @param block
     *            The {@link CoordinatesBlock} to convert
     * @param pointsBlockSize
     *            The amount of points for each {@link PointsBlock}
     * @param priority
     *            The priority of this task.
     * @param manager
     *            The {@link PointsBlockManager} to use to allocate the {@link PointsBlock}s
     */
    public LinearCoordinateToPointsBlockConverter(ExecutorService executor, int priority,
    JobBuilder<PointsBlock> jobBuilder, CoordinatesBlock block, int pointsBlockSize, PointsBlockManager manager)
    {
        super(executor, priority, jobBuilder);
        this.block = block;
        this.pointsBlockSize = pointsBlockSize;
        this.pointsBlockManager = manager;
        lastX = block.minX;
        lastY = block.minY;
    }
    
    @Override
    public PointsBlock nextStep()
    {
        if (lastY >= block.maxY)
        {
            return null;
        }
        int points = 0;
        double x = lastX;
        double y = lastY;
        pointsBlock = pointsBlockManager.allocatePointsBlock(pointsBlockSize);
        while (points < pointsBlockSize)
        {
            pointsBlock.real[points] = x;
            pointsBlock.imag[points] = y;
            points++;
            
            x += block.stepX;
            if (x >= block.maxX)
            {
                x = block.minX;
                y += block.stepY;
                if (y >= block.maxY)
                {
                    break;
                }
            }
        }
        
        lastX = x;
        lastY = y;
        // normal case
        if (points != pointsBlockSize)
        /*
         * this can be the case only for the last block: copy the data in a block of the appropriate size before
         * returning
         */
        {
            PointsBlock pointsBlock2 = pointsBlockManager.allocatePointsBlock(points);
            for (int i = 0; i < points; ++i)
            {
                pointsBlock2.real[i] = pointsBlock.real[i];
                pointsBlock2.imag[i] = pointsBlock.imag[i];
            }
            pointsBlock.release();
            pointsBlock = pointsBlock2;
        }
        return pointsBlock;
    }
    
}