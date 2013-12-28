package net.lab0.nebula.project;

import java.util.ArrayList;
import java.util.List;

/**
 * Meta information about a quad tree.
 */
public class QuadTreeInformation
{
    public int           id                         = -1;
    public long          maximumIterationCount      = -1;
    public long          maximumIterationDifference = -1;
    public int           pointsCountPerSide         = -1;
    public int           maxDepth                   = -1;
    public int           nodesCount                 = -1;
    
    public List<Integer> blockSizes                 = new ArrayList<>();
}
