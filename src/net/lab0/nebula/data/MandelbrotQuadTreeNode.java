package net.lab0.nebula.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import net.lab0.nebula.enums.PositionInParent;
import net.lab0.nebula.enums.Status;

public class MandelbrotQuadTreeNode
{
    public static class Coords
    {
        private double min;
        private double max;
        
        public Coords(double min, double max)
        {
            super();
            this.min = min;
            this.max = max;
        }
        
        public double getMin()
        {
            return min;
        }
        
        public double getMax()
        {
            return max;
        }
    }
    
    public static class NodePath
    {
        private int    depth;
        private BitSet path;
        
        public NodePath(int depth, BitSet path)
        {
            super();
            this.depth = depth;
            this.path = path;
        }
        
        public int getDepth()
        {
            return depth;
        }
        
        public BitSet getPath()
        {
            return path;
        }
    }
    
    /**
     * Factory to create nodes directly
     * 
     * @author 116
     * 
     */
    public static class Factory
    {
        /**
         * Builds a node with net.lab0.nebula.data.MandelbrotQuadTreeNode.Factory#positionToDepthAndBitSetPath(String
         * stringPath)
         */
        public static MandelbrotQuadTreeNode buildNode(String stringPath)
        {
            return new MandelbrotQuadTreeNode(MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(stringPath));
        }
        
        /**
         * Builds a node with
         * net.lab0.nebula.data.MandelbrotQuadTreeNode.Factory#positionToDepthAndBitSetPath(PositionInParent...
         * positions)
         */
        public static MandelbrotQuadTreeNode buildNode(PositionInParent... positions)
        {
            return new MandelbrotQuadTreeNode(MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(positions));
        }
        
        /**
         * Builds a node with
         * net.lab0.nebula.data.MandelbrotQuadTreeNode.Factory#positionToDepthAndBitSetPath(PositionInParent...
         * positions) by concatenating the parent and child paths.
         */
        public static MandelbrotQuadTreeNode buildNode(PositionInParent[] parentPath, PositionInParent childPosition)
        {
            PositionInParent[] fullPath = Arrays.copyOf(parentPath, parentPath.length + 1);
            fullPath[parentPath.length] = childPosition;
            return new MandelbrotQuadTreeNode(MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(fullPath));
        }
        
        /**
         * Builds a root node. Equivalent to buildNode(PositionInParent.Root)
         */
        public static MandelbrotQuadTreeNode buildRoot()
        {
            return new MandelbrotQuadTreeNode(
            MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(PositionInParent.Root));
        }
    }
    
    /**
     * The depth of this node. Root's depth = 0
     */
    public final int    depth;
    /**
     * The path of this node. The first 2 bits of this array are undefined.
     * 
     * <pre>
     * +--+--+
     * |00|01|
     * +--+--+
     * |10|11|
     * +--+--+
     * </pre>
     */
    public final BitSet path;
    
    public long         minimumIteration = -1;
    public long         maximumIteration = -1;
    public Status       status           = Status.VOID;
    
    public MandelbrotQuadTreeNode(int depth)
    {
        if (depth < 0)
        {
            throw new IllegalArgumentException("The depth must be positive or 0. depth=" + depth);
        }
        this.depth = depth;
        this.path = new BitSet(depth * 2);
    }
    
    protected MandelbrotQuadTreeNode(int depth, BitSet path)
    {
        if (depth < 0)
        {
            throw new IllegalArgumentException("The depth must be positive or 0. depth=" + depth);
        }
        if (path.size() < depth * 2)
        {
            throw new IllegalArgumentException("The path and the depth don't match: depth=" + depth + ", path.length="
            + path.size() + ". The path size must be at least depth*2");
        }
        this.depth = depth;
        this.path = path;
    }
    
    public MandelbrotQuadTreeNode(NodePath path)
    {
        this(path.getDepth(), path.getPath());
    }
    
    public MandelbrotQuadTreeNode(int depth, BitSet path, long minimumIteration, long maximumIteration)
    {
        this.depth = depth;
        this.path = path;
        this.minimumIteration = minimumIteration;
        this.maximumIteration = maximumIteration;
    }
    
    /**
     * Computes the position of minX and maxX for this node using the path
     * 
     * @return a {@link Coords}: minX et maxX
     */
    public Coords getX()
    {
        double minX = -2.0;
        double maxX = 2.0;
        for (int i = 1; i <= depth; ++i)
        {
            // the current position in path is indicated by the bits depth*2 and depth*2+1 in path[]
            if (path.get(2 * i + 1)) // at the left
            {
                minX = (minX + maxX) / 2;
            }
            else
            {
                maxX = (minX + maxX) / 2;
            }
        }
        return new Coords(minX, maxX);
    }
    
    /**
     * Computes the position of minY and maxY for this node using the path
     * 
     * @return a Coords: minY et maxY
     */
    public Coords getY()
    {
        double minY = -2.0;
        double maxY = 2.0;
        for (int i = 1; i <= depth; ++i)
        {
            // the current position in path is indicated by the bits depth*2 and depth*2+1 in path[]
            if (path.get(2 * i)) // at the bottom
            {
                maxY = (minY + maxY) / 2;
            }
            else
            {
                minY = (minY + maxY) / 2;
            }
        }
        return new Coords(minY, maxY);
    }
    
    public MandelbrotQuadTreeNode[] split()
    {
        MandelbrotQuadTreeNode[] splitted = new MandelbrotQuadTreeNode[4];
        PositionInParent[] positions = this.getPathAsEnum();
        splitted[0] = MandelbrotQuadTreeNode.Factory.buildNode(positions, PositionInParent.TopLeft);
        splitted[1] = MandelbrotQuadTreeNode.Factory.buildNode(positions, PositionInParent.TopRight);
        splitted[2] = MandelbrotQuadTreeNode.Factory.buildNode(positions, PositionInParent.BottomLeft);
        splitted[3] = MandelbrotQuadTreeNode.Factory.buildNode(positions, PositionInParent.BottomRight);
        return splitted;
    }
    
    public static NodePath positionToDepthAndBitSetPath(String stringPath)
    {
        List<PositionInParent> positions = new ArrayList<>(stringPath.length());
        for (char c : stringPath.toCharArray())
        {
            if (c != 'R')
            {
                positions.add(PositionInParent.values()[Integer.parseInt("" + c)]);
            }
            else
            {
                positions.add(PositionInParent.Root);
            }
        }
        return positionToDepthAndBitSetPath(positions.toArray(new PositionInParent[0]));
    }
    
    public static NodePath positionToDepthAndBitSetPath(PositionInParent... positions)
    {
        BitSet path = new BitSet(positions.length * 2);
        int index = 0;
        if (positions.length == 0)
        {
            throw new IllegalArgumentException("The path must contain at least 1 element:the root");
        }
        if (positions[0] != PositionInParent.Root)
        {
            throw new IllegalArgumentException("The first position in the path must be PositionInParent.Root");
        }
        for (PositionInParent p : positions)
        {
            switch (p)
            {
                case TopLeft:
                    // 00
                    break;
                case TopRight:
                    // 01
                    path.set(index + 1, true);
                    break;
                case BottomLeft:
                    // 10
                    path.set(index, true);
                    break;
                case BottomRight:
                    // 11
                    path.set(index, true);
                    path.set(index + 1, true);
                    break;
                case Root:
                    // case not handled: this must never happen after the first node
                    if (index != 0)
                    {
                        throw new IllegalArgumentException(
                        "The PositionInParent.Root can and must only be used for the first element of a path");
                    }
                default:
                    assert (false);
                    break;
            }
            index += 2;
        }
        return new NodePath(positions.length - 1, path);
    }
    
    public PositionInParent[] getPathAsEnum()
    {
        PositionInParent[] positions = new PositionInParent[depth + 1];
        positions[0] = PositionInParent.Root;
        for (int i = 1; i <= depth; ++i)
        {
            int val = 0;
            if (path.get(2 * i))
            {
                val += 2;
            }
            if (path.get(2 * i + 1))
            {
                val += 1;
            }
            
            switch (val)
            {
                case 0:
                    positions[i] = PositionInParent.TopLeft;
                    break;
                case 1:
                    positions[i] = PositionInParent.TopRight;
                    break;
                case 2:
                    positions[i] = PositionInParent.BottomLeft;
                    break;
                case 3:
                    positions[i] = PositionInParent.BottomRight;
                    break;
                
                default:
                    assert (false);
                    break;
            }
        }
        return positions;
    }
    
    private String getPathAsString()
    {
        StringBuilder sb = new StringBuilder();
        PositionInParent[] positions = getPathAsEnum();
        for (PositionInParent p : positions)
        {
            switch (p)
            {
                case TopLeft:
                    sb.append("0");
                    break;
                case TopRight:
                    sb.append("1");
                    break;
                case BottomLeft:
                    sb.append("2");
                    break;
                case BottomRight:
                    sb.append("3");
                    break;
                case Root:
                    sb.append("R");
                    break;
                
                default:
                    assert (false);
                    break;
            }
        }
        return sb.toString();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("depth=").append(depth).append(" - path=" + getPathAsString());
        return sb.toString();
    }
}
