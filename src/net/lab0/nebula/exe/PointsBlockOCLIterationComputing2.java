package net.lab0.nebula.exe;

import java.nio.IntBuffer;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.OpenCLManager;
import net.lab0.nebula.project.PointsComputingParameters;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.SimpleJob;

/**
 * Scatters a {@link CoordinatesBlock} and computes the iterations of the resulting points using CPU computing power.
 * Outputs the points validated by a filter.
 * 
 * @author 116
 * 
 */
public class PointsBlockOCLIterationComputing2
extends SimpleJob<CoordinatesBlock, PointsBlock>
{
    private PointsComputingParameters parameters;
    
    public PointsBlockOCLIterationComputing2(CascadingJob<?, CoordinatesBlock> parent,
    JobBuilder<PointsBlock> jobBuilder, CoordinatesBlock block, PointsComputingParameters parameters)
    {
        super(parent, jobBuilder, block);
        this.parameters = parameters;
    }
    
    @Override
    public PointsBlock singleStep(CoordinatesBlock block)
    {
        long pointsOnSides = parameters.getPointsPerSideAtRootLevel();
        double step = 4.0 / pointsOnSides;
        // assume that this block is a square
        int points = (int) ((block.maxX - block.minX) / step);
        if (points > Math.sqrt(Integer.MAX_VALUE))
        {
            throw new IllegalArgumentException("There are too many points on the side of a node: " + points);
        }
        int sliceSize = points * points;
        double[] x = new double[sliceSize];
        double[] y = new double[sliceSize];
        
        fillArrays(block, step, points, x, y);
        
        IntBuffer result = OpenCLManager.getInstance().compute(x, y, parameters.getMaximumIteration());
        result.rewind();
        
        int returnedPointsCount = getValidPoints(result);
        
//        System.out.println(returnedPointsCount);
        
        PointsBlock returned = copyValidPoints(x, y, result, returnedPointsCount);
        
        return returned;
    }
    
    private PointsBlock copyValidPoints(double[] x, double[] y, IntBuffer result, int returnedPointsCount)
    {
        result.rewind();
        PointsBlock returned = new PointsBlock(returnedPointsCount);
        int currentSrcIndex = 0;
        int currentDstIndex = 0;
        while (result.hasRemaining())
        {
            int iterations = result.get();
            if (parameters.getFilter().apply((long) iterations))
            {
                returned.real[currentDstIndex] = x[currentSrcIndex];
                returned.imag[currentDstIndex] = y[currentSrcIndex];
                returned.iter[currentDstIndex] = iterations;
                currentDstIndex++;
            }
            currentSrcIndex++;
        }
        return returned;
    }
    
    private int getValidPoints(IntBuffer result)
    {
        int returnedPointsCount = 0;
        while (result.hasRemaining())
        {
            int iterations = result.get();
            if (parameters.getFilter().apply((long) iterations))
            {
                returnedPointsCount++;
            }
        }
        return returnedPointsCount;
    }
    
    private void fillArrays(CoordinatesBlock block, double step, int pointsOnSides, double[] x,
    double[] y)
    {
        for (int k = 0; k < pointsOnSides; ++k)
        {
            double yCurrent = block.minY + step * k;
            for (int j = 0; j < pointsOnSides; ++j)
            {
                int index = k * pointsOnSides + j;
                x[index] = block.minX + j * step;
                y[index] = yCurrent;
            }
        }
    }
}
