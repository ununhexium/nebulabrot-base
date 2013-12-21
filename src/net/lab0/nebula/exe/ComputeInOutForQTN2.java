package net.lab0.nebula.exe;

import static java.lang.Math.max;
import static java.lang.Math.min;
import net.lab0.nebula.core.MandelbrotComputeRoutines;
import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.MandelbrotQuadTreeNode.Coords;
import net.lab0.nebula.enums.Status;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.SimpleJob;

//TODO: finish + doc
public class ComputeInOutForQTN2
extends SimpleJob<MandelbrotQuadTreeNode, MandelbrotQuadTreeNode>
{
    private long maximumIteration;
    private int  sidePointsCount;
    private long iterationDifferenceLimit;
    
    /**
     * 
     * @param parent
     * @param jobBuilder
     * @param input
     * @param maximumIteration
     * @param sidePointsCount
     * @param iterationDifferenceLimit
     *            The maximum gap between the minimum amount of iteration and the maximum amount of iteration there can
     *            be to still consider that this node is outside the Mandelbrot set. If the iteration difference
     *            strictly exceeds this amount, the node is not considered outside the Mandelbrot set.
     */
    public ComputeInOutForQTN2(CascadingJob<?, MandelbrotQuadTreeNode> parent,
    JobBuilder<MandelbrotQuadTreeNode> jobBuilder, MandelbrotQuadTreeNode input, long maximumIteration,
    int sidePointsCount, long iterationDifferenceLimit)
    {
        super(parent, jobBuilder, input);
        this.maximumIteration = maximumIteration;
        this.sidePointsCount = sidePointsCount;
        this.iterationDifferenceLimit = iterationDifferenceLimit;
    }
    
    @Override
    public MandelbrotQuadTreeNode singleStep(MandelbrotQuadTreeNode node)
    {
        // get the bounds of the node
        Coords X = node.getX();
        double minX = X.getMin();
        double maxX = X.getMax();
        Coords Y = node.getY();
        double minY = Y.getMin();
        double maxY = Y.getMax();
        
        node.status = cornerTest(minX, maxX, minY, maxY);
        if (node.status == Status.BROWSED)
        {
            return node;
        }
        // here, the node can be inside, outside or browsed
        node.status = edgeCompute(minX, maxX, minY, maxY);
        return node;
    }
    
    /**
     * Computes the Mandelbrot formula for the 4 corner points and tries to determine the status of this node.
     * 
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     * @return the node status if this function was able to determine it. <code>null</code> otherwise
     */
    private Status cornerTest(double minX, double maxX, double minY, double maxY)
    {
        long it1 = MandelbrotComputeRoutines.computeIterationsCountOptim2(minX, minY, maximumIteration);
        long it2 = MandelbrotComputeRoutines.computeIterationsCountOptim2(minX, maxY, maximumIteration);
        long it3 = MandelbrotComputeRoutines.computeIterationsCountOptim2(maxX, minY, maximumIteration);
        long it4 = MandelbrotComputeRoutines.computeIterationsCountOptim2(maxX, maxY, maximumIteration);
        
        long min = min(min(it1, it2), min(it3, it4));
        long max = max(max(it1, it2), max(it3, it4));
        
        if ((max - min) <= iterationDifferenceLimit && min < maximumIteration)
        {
            // we need further investigation
            return null;
        }
        else
        {
            // this node has the status browsed for sure
            return Status.BROWSED;
        }
    }
    
    /**
     * Tests if this node is completely inside the mandelbrot's set. The node is considered inside the Mandelbrot set is
     * all edge points are inside the mandelbrot set.
     */
    private Status edgeCompute(double minX, double maxX, double minY, double maxY)
    {
        long min;
        long max;
        min = max = MandelbrotComputeRoutines.computeIterationsCountOptim2(minX, minY, maximumIteration);
        double step = (maxX - minX) / (double) (sidePointsCount - 1);
        
        // bottom side of the rectangle
        for (int i = 0; i < sidePointsCount; i++)
        {
            double real = (minX + (double) i * step);
            double img = minY;
            
            long current = MandelbrotComputeRoutines.computeIterationsCountOptim2(real, img, maximumIteration);
            min = Math.min(min, current);
            max = Math.max(max, current);
            if (max - min > iterationDifferenceLimit)
            {
                return Status.BROWSED;
            }
        }
        
        // top side of the rectangle
        for (int i = 0; i < sidePointsCount; ++i)
        {
            double real = (minX + (double) i * step);
            double img = maxY;
            
            long current = MandelbrotComputeRoutines.computeIterationsCountOptim2(real, img, maximumIteration);
            min = Math.min(min, current);
            max = Math.max(max, current);
            if (max - min > iterationDifferenceLimit)
            {
                return Status.BROWSED;
            }
        }
        
        // left side of the rectangle
        for (int i = 0; i < sidePointsCount; ++i)
        {
            double real = (minX);
            double img = (minY + (double) i * step);
            
            long current = MandelbrotComputeRoutines.computeIterationsCountOptim2(real, img, maximumIteration);
            min = Math.min(min, current);
            max = Math.max(max, current);
            if (max - min > iterationDifferenceLimit)
            {
                return Status.BROWSED;
            }
        }
        
        // bottom side of the rectangle
        for (int i = 0; i < sidePointsCount; ++i)
        {
            double real = (maxX);
            double img = (minY + (double) i * step);
            
            long current = MandelbrotComputeRoutines.computeIterationsCountOptim2(real, img, maximumIteration);
            min = Math.min(min, current);
            max = Math.max(max, current);
            if (max - min > iterationDifferenceLimit)
            {
                return Status.BROWSED;
            }
        }
        
        if (max - min <= iterationDifferenceLimit && max < maximumIteration)
        {
            return Status.OUTSIDE;
        }
        else if (min >= maximumIteration)
        {
            return Status.INSIDE;
        }
        else
        {
            /*
             * The difference is small but the minimum is not at least equal to maximum iteration. The only case where
             * we have this is when we reach the end of the computation for the tree. Very unlikely if max iteration is
             * big (let's say big is >2^16). In any case, it means that we can't continue the computation because it may
             * be outside or browsed.
             */
            return Status.VOID;
        }
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
