package net.lab0.nebula.project;

public class PointInformation
{
    public int              id;
    public int              treeId;
    public long             maximumIteration;
    public long             pointsPerSideAtRootLevel;
    public int              maxDepth;
    public int              blockSize;
    public ComputingRoutine routine;
    public long             minimumIteration;
    
    public PointInformation()
    {
        
    }
    
    public PointInformation(int id, PointsComputingParameters parameters)
    {
        this.id = id;
        this.treeId = parameters.getTreeId();
        this.maximumIteration = parameters.getMaximumIteration();
        this.pointsPerSideAtRootLevel = parameters.getPointsPerSideAtRootLevel();
        this.maxDepth = parameters.getMaxDepth();
        this.blockSize = parameters.getBlockSize();
        this.routine = parameters.getRoutine();
        this.minimumIteration = parameters.getMinimumIteration();
    }
}
