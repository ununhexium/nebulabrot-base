package net.lab0.nebula.project;

import com.google.common.base.Predicate;

public class PointsComputingParameters
{
    private int              treeId;
    private long             maximumIteration;
    private long             pointsPerSideAtRootLevel;
    private int              maxDepth         = Integer.MAX_VALUE;
    private int              blockSize        = -1;
    private ComputingRoutine routine          = ComputingRoutine.CPU;
    private long             minimumIteration = 0;
    
    /**
     * 
     * @param treeId
     *            The id of the tree that must be used.
     * @param maximumIteration 
     * @param pointsPerSideAtRootLevel
     *            The number of points on the side of the root node expressed as a power of 2.
     * @throws IllegalArgumentException
     *             If the given number of points is not a power of 2.
     */
    public PointsComputingParameters(int treeId, long maximumIteration, long pointsPerSideAtRootLevel)
    throws IllegalArgumentException
    {
        super();
        if (Long.bitCount(pointsPerSideAtRootLevel) != 1 || pointsPerSideAtRootLevel < 0)
        {
            throw new IllegalArgumentException("The points count (" + pointsPerSideAtRootLevel
            + ") must be a positive power of 2.");
        }
        this.treeId = treeId;
        this.pointsPerSideAtRootLevel = pointsPerSideAtRootLevel;
        this.maximumIteration = maximumIteration;
    }
    
    public long getPointsPerSideAtRootLevel()
    {
        return pointsPerSideAtRootLevel;
    }
    
    public int getMaxDepth()
    {
        return maxDepth;
    }
    
    /**
     * Limit the use of the quad tree to this depth (inclusive).
     * 
     * @param maxDepth
     */
    public void setMaxDepth(int maxDepth)
    {
        this.maxDepth = maxDepth;
    }
    
    public int getBlockSize()
    {
        return blockSize;
    }
    
    /**
     * The block size to use to do the computation.
     * 
     * @param blockSize
     */
    public void setBlockSize(int blockSize)
    {
        this.blockSize = blockSize;
    }
    
    public int getTreeId()
    {
        return treeId;
    }
    
    public ComputingRoutine getRoutine()
    {
        return routine;
    }
    
    public void setRoutine(ComputingRoutine routine)
    {
        this.routine = routine;
    }
    
    public long getMaximumIteration()
    {
        return maximumIteration;
    }
    
    public void setMaximumIteration(long maximumIteration)
    {
        this.maximumIteration = maximumIteration;
    }
    
    public void setTreeId(int treeId)
    {
        this.treeId = treeId;
    }
    
    public void setPointsPerSideAtRootLevel(long pointsPerSideAtRootLevel)
    {
        this.pointsPerSideAtRootLevel = pointsPerSideAtRootLevel;
    }
    
    public long getMinimumIteration()
    {
        return minimumIteration;
    }
    
    public void setMinimumIteration(long minimumIteration)
    {
        this.minimumIteration = minimumIteration;
    }
    
    public Predicate<Long> getFilter()
    {
        return new Predicate<Long>()
        {
            @Override
            public boolean apply(Long input)
            {
                long i = input;
                return (i < getMaximumIteration() && i >= getMinimumIteration());
            }
        };
    }
    
    public boolean isBlockSizeSpecified()
    {
        return blockSize > 0;
    }
    
    public double getStep(int depth)
    {
        return 4.0 / (pointsPerSideAtRootLevel >> depth);
    }
}
