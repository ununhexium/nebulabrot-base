package net.lab0.nebula.project;

import java.util.ArrayList;
import java.util.List;

public class QuadTreesInformation
{
    public List<QuadTreeInformation> quadTrees = new ArrayList<>();
    
    /**
     * 
     * @param treeId
     * @return <code>true</code> if the tree with the given id is available
     */
    public boolean hasQuadTree(int treeId)
    {
        for (QuadTreeInformation qt : quadTrees)
        {
            if (qt.id == treeId)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param treeId
     *            The id of the tree you want to get
     * @return The quad tree information related to the tree with the given id.
     */
    public QuadTreeInformation getById(int treeId)
    {
        for (QuadTreeInformation tree : quadTrees)
        {
            if (tree.id == treeId)
            {
                return tree;
            }
        }
        return null;
    }
}
