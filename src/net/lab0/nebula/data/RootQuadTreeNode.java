package net.lab0.nebula.data;

/**
 * This special quad tree node stores its own bounds instead of computing them.
 * 
 * @author 116@lab0.net
 * 
 */
public class RootQuadTreeNode
extends StatusQuadTreeNode
{
    /**
     * bounds of this node
     */
    public double minX, maxX, minY, maxY;
    
    /**
     * Wraps the given quad tree node as a root node.
     * 
     * @param abstractQuadTreeNode
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    public RootQuadTreeNode(StatusQuadTreeNode abstractQuadTreeNode, double minX, double maxX, double minY, double maxY)
    {
        super(null);
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.children = abstractQuadTreeNode.children;
        this.status = abstractQuadTreeNode.status;
        
        for(int i=0; i<4; ++i)
        {
            abstractQuadTreeNode.children[i].parent = this;
        }
    }
    
    public RootQuadTreeNode(double minX, double maxX, double minY, double maxY)
    {
        super();
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
    
    @Override
    public String toString()
    {
        return "RootQuadTreeNode [minX=" + getMinX() + ", maxX=" + getMaxX() + ", minY=" + getMinY() + ", maxY=" + getMaxY() + ", status=" + status + "]";
    }

    @Override
    public double getMinX()
    {
        return minX;
    }

    @Override
    public double getMaxX()
    {
        return maxX;
    }

    @Override
    public double getMinY()
    {
        return minY;
    }

    @Override
    public double getMaxY()
    {
        return maxY;
    }
    
}
