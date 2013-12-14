package net.lab0.nebula.exe;

import java.nio.IntBuffer;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.OpenCLManager;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.Splitter;

public class PointsBlockOCLIterationComputing2
extends Splitter<CoordinatesBlock, PointsBlock>
{
    public static class Parameters
    {
        // inclusive
        public long maximumIteration;
        // inclusive
        public long minimumIteration;
        public int  pointsOnSides;
        public int  blocks;
        
        /**
         * 
         * @param maximumIteration
         *            inclusive
         * @param minimumIteration
         *            inclusive
         * @param pointsOnSides
         *            points on each side of the square
         * @param blocks
         *            number of blocks to split the computation into
         * 
         * @throws IllegalArgumentException
         *             if pointsOnSide is not a multiple of blocks
         */
        public Parameters(long maximumIteration, long minimumIteration, int pointsOnSides, int blocks)
        {
            super();
            
            if (pointsOnSides % blocks != 0)
            {
                throw new IllegalArgumentException("pointsOnSides=" + pointsOnSides + " must be a multiple of "
                + blocks);
            }
            
            this.maximumIteration = maximumIteration;
            this.minimumIteration = minimumIteration;
            this.pointsOnSides = pointsOnSides;
            this.blocks = blocks;
        }
        
    }
    
    private CoordinatesBlock block;
    private int              stepIndex;
    private Parameters       parameters;
    
    public PointsBlockOCLIterationComputing2(CascadingJob<?, CoordinatesBlock> parent,
    JobBuilder<PointsBlock> jobBuilder, CoordinatesBlock block, Parameters parameters)
    {
        super(parent, jobBuilder);
        this.block = block;
        this.parameters = parameters;
    }
    
    @Override
    public PointsBlock nextStep()
    throws Exception
    {
        double step = (block.maxX - block.minX) / parameters.pointsOnSides;
        int sliceSize = parameters.pointsOnSides * parameters.pointsOnSides / parameters.blocks;
        double[] x = new double[sliceSize];
        double[] y = new double[sliceSize];
        
        fillArrays(step, parameters.pointsOnSides / parameters.blocks, x, y);
        
        IntBuffer result = OpenCLManager.getInstance().compute(x, y, parameters.maximumIteration);
        result.rewind();
        
        int returnedPointsCount = getValidPoints(result);
        
        PointsBlock returned = copyValidPoints(x, y, result, returnedPointsCount);
        
        stepIndex++;
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
            if (iterations >= parameters.minimumIteration && iterations <= parameters.maximumIteration)
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
            if (iterations >= parameters.minimumIteration && iterations <= parameters.maximumIteration)
            {
                returnedPointsCount++;
            }
        }
        return returnedPointsCount;
    }
    
    private void fillArrays(double step, int sliceSize, double[] x, double[] y)
    {
        int kStart = stepIndex * sliceSize;
        int kEnd = (stepIndex + 1) * sliceSize;
        for (int k = kStart; k < kEnd; ++k)
        {
            int kBase = k - kStart;
            double yCurrent = block.minY + step * k;
            for (int j = 0; j < parameters.pointsOnSides; ++j)
            {
                int index = kBase * parameters.pointsOnSides + j;
                x[index] = block.minX + j * step;
                y[index] = yCurrent;
            }
        }
    }
    
    @Override
    public boolean hasNext()
    {
        return stepIndex < parameters.blocks;
    }
}
