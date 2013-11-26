package net.lab0.nebula.exe;

import net.lab0.nebula.core.MandelbrotComputeRoutines;
import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.MandelbrotQuadTreeNode.Coords;
import net.lab0.nebula.enums.Status;
import net.lab0.tools.Pair;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.exec.SimpleJob;

public class ComputeInOutForQuadTreeNode
extends SimpleJob<MandelbrotQuadTreeNode, MandelbrotQuadTreeNode>
{
    private long maximumIteration;
    private int  sidePointsCount;
    private long iterationDifferenceLimit;
    
    public ComputeInOutForQuadTreeNode(PriorityExecutor executor, int priority,
    JobBuilder<MandelbrotQuadTreeNode> jobBuilder, MandelbrotQuadTreeNode input, long maximumIteration,
    int sidePointsCount, long iterationDifferenceLimit)
    {
        super(executor, priority, jobBuilder, input);
        this.maximumIteration = maximumIteration;
        this.sidePointsCount = sidePointsCount;
        this.iterationDifferenceLimit = iterationDifferenceLimit;
    }
    
    @Override
    public MandelbrotQuadTreeNode singleStep(MandelbrotQuadTreeNode input)
    {
        if (isInsideMandelbrotSet(input, sidePointsCount, maximumIteration))
        {
            input.status = Status.INSIDE;
        }
        else
        {
            computeOutsideMandelbrotSet(input, sidePointsCount, maximumIteration, iterationDifferenceLimit);
        }
        return input;
    }
    
    /**
     * Tests if this node is completely inside the mandelbrot's set. The node is considered inside the Mandelbrot set is
     * all edge points are inside the mandelbrot set.
     */
    private boolean isInsideMandelbrotSet(MandelbrotQuadTreeNode node, int sidePointsCount, long maxIter)
    {
        // get the bounds of the node
        Coords X = node.getX();
        double minX = X.getMin();
        double maxX = X.getMax();
        Coords Y = node.getY();
        double minY = Y.getMin();
        double maxY = Y.getMax();
        
        double step = (maxX - minX) / (double) (sidePointsCount - 1);
        
        // bottom side of the rectangle
        for (int i = 0; i < sidePointsCount; i++)
        {
            double real = (minX + (double) i * step);
            double img = minY;
            
            if (MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim2(real, img, maxIter))
            {
                return false;
            }
        }
        
        // top side of the rectangle
        for (int i = 0; i < sidePointsCount; ++i)
        {
            double real = (minX + (double) i * step);
            double img = maxY;
            
            if (MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim2(real, img, maxIter))
            {
                return false;
            }
        }
        
        // left side of the rectangle
        for (int i = 0; i < sidePointsCount; ++i)
        {
            double real = (minX);
            double img = (minY + (double) i * step);
            
            if (MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim2(real, img, maxIter))
            {
                return false;
            }
        }
        
        // bottom side of the rectangle
        for (int i = 0; i < sidePointsCount; ++i)
        {
            double real = (maxX);
            double img = (minY + (double) i * step);
            
            if (MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim2(real, img, maxIter))
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Sets the status, min and max iteration attributes to the node.
     */
    private void computeOutsideMandelbrotSet(MandelbrotQuadTreeNode node, int sidePointsCount, long maxIter, long diff)
    {
        // get the bounds of the node
        Coords X = node.getX();
        double minX = X.getMin();
        double maxX = X.getMax();
        Coords Y = node.getY();
        double minY = Y.getMin();
        double maxY = Y.getMax();
        
        long min, max;
        // init min and max iter
        {
            double real = minX;
            double img = minY;
            min = max = MandelbrotComputeRoutines.computeIterationsCountOptim2(real, img, maxIter);
        }
        
        double stepX = (maxX - minX) / (double) (sidePointsCount);
        double stepY = (maxY - minY) / (double) (sidePointsCount);
        
        // bottom side of the rectangle
        for (int i = 0; i <= sidePointsCount; i++)
        {
            double real = minX + (double) i * stepX;
            for (int j = 0; j <= sidePointsCount; ++j)
            {
                double img = (minY + (double) j * stepY);
                
                long iter = MandelbrotComputeRoutines.computeIterationsCountOptim2(real, img, maxIter);
                
                if (iter < min)
                {
                    min = iter;
                }
                else if (iter > max)
                {
                    max = iter;
                }
                
                if ((max - min) > diff)
                {
                    node.status = Status.BROWSED;
                    return;
                }
            }
        }
        
        node.status = Status.OUTSIDE;
        node.minimumIteration = min;
        node.maximumIteration = max;
    }
    
}
