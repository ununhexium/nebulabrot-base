package net.lab0.nebula.exe;

import java.util.concurrent.ExecutorService;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.PointsBlockManager;
import net.lab0.tools.exec.CascadingPrioritizedRunnable;

/**
 * Converts a {@link CoordinatesBlock} into as many {@link PointsBlock} as needed.
 * 
 * @author 116@lab0.net
 * 
 */
public class LinearPointsBlockGenerator
extends CascadingPrioritizedRunnable
{
    private CoordinatesBlock   block;
    private int                pointsBlockSize;
    private double             lastX;
    private double             lastY;
    private PointsBlockManager pointsBlockManager;
    
    /**
     * 
     * @param executor
     *            The executor in which this job has to be executed
     * @param block
     *            The block to convert
     * @param pointsBlockSize
     *            The amount of points for each {@link PointsBlock}
     * @param priority
     *            The priority of this task.
     * @param pointsBlockManager
     *            The {@link PointsBlockManager} to use to allocate the {@link PointsBlock}s
     */
    public LinearPointsBlockGenerator(ExecutorService executor, CoordinatesBlock block, int pointsBlockSize,
    PointsBlockManager pointsBlockManager, int priority)
    {
        super(executor, priority);
        this.block = block;
        this.pointsBlockSize = pointsBlockSize;
        this.pointsBlockManager = pointsBlockManager;
        lastX = block.minX;
        lastY = block.minY;
    }
    
    @Override
    public void executeTask()
    {
        // converts the coordinates block to points block
        int points = 0;
        double x = lastX;
        double y = lastY;
        PointsBlock pointsBlock = pointsBlockManager.allocatePointsBlock(pointsBlockSize);
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
    }
    
    @Override
    public CascadingPrioritizedRunnable[] nextJobs()
    {
        return null;
    }
    
}
