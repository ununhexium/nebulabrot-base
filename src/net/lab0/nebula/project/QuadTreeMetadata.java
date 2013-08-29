package net.lab0.nebula.project;

import java.io.File;
import java.nio.file.Path;

import net.lab0.nebula.enums.Status;

public class QuadTreeMetadata
{
    /**
     * the number of points per side for each node
     */
    private int     pointsPerSide;
    
    /**
     * the maximum number of iterations to do
     */
    private int     maxIter;
    
    /**
     * the maximum number of iterations difference to consider a node as {@link Status}.OUTSIDE
     */
    private int     diffIterLimit;
    
    /**
     * the max depth of computation for this tree
     */
    private int     maxDepth;
    
    /**
     * The folder where this tree is saved.
     */
    private Path    folder;
    
    /**
     * The location of the serialised data.
     */
    private Path    dataFile;
    
    /**
     * If the binary data is indexed.
     */
    private boolean indexed;
    
    public QuadTreeMetadata(int pointsPerSide, int maxIter, int diffIterLimit, int maxDepth)
    {
        super();
        this.pointsPerSide = pointsPerSide;
        this.maxIter = maxIter;
        this.diffIterLimit = diffIterLimit;
        this.maxDepth = maxDepth;
    }
    
    public int getPointsPerSide()
    {
        return pointsPerSide;
    }
    
    public int getMaxIter()
    {
        return maxIter;
    }
    
    public int getDiffIterLimit()
    {
        return diffIterLimit;
    }
    
    public int getMaxDepth()
    {
        return maxDepth;
    }
    
}
