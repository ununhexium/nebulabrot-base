package net.lab0.nebula.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import net.lab0.nebula.enums.PositionInParent;
import net.lab0.nebula.enums.Status;

/**
 * A quad-tree node specifically design for the computation of the Mandelbrot set. Assumes that the root node has the
 * coordinates x=[-2.0;2.0], y=[-2.0,2.0].
 * 
 * @author 116
 * 
 */
public class MandelbrotQuadTreeNode
{
    /**
     * The min/max coordinates. Replacement for the general Pair&lt;Double,Double&gt; class.
     * 
     * @author 116
     * 
     */
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
    
    /**
     * Represent the path of a node in the quad tree.
     * 
     * @author 116
     * 
     */
    public static class NodePath
    {
        /**
         * The depth of the node. Root's depth = 0.
         */
        public final int    depth;
        /**
         * The path of the node. The first 2 bits of this array (root node position) are undefined. The bits must be
         * considered by groups of 2. The value of the grouping indicates the position of the node in its parent. The
         * grouping ordinal value (0-based) is the depth at which it applies.
         * 
         * <table>
         * <tbody>
         * <tr>
         * <td></td>
         * <td>Left</td>
         * <td>Right</td>
         * </tr>
         * <tr>
         * <td>Top</td>
         * <td>00</td>
         * <td>01</td>
         * </tr>
         * <tr>
         * <td>Bottom</td>
         * <td>10</td>
         * <td>11</td>
         * </tr>
         * </tbody>
         * </table>
         * 
         */
        
        // +--+--+
        // |00|01|
        // +--+--+
        // |10|11|
        // +--+--+
        public final BitSet path;
        
        /**
         * Creates a node with the given parameters
         * 
         * @param depth
         *            The depth of the node. Must be positive.
         * @param path
         *            The path of the node. Not <code>null</code>.
         */
        public NodePath(int depth, BitSet path)
        {
            super();
            if (depth < 0)
            {
                throw new IllegalArgumentException("The depth=" + depth + " must be positive or 0.");
            }
            if (path == null)
            {
                throw new IllegalArgumentException("The path must not be null.");
            }
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
     * @param stringPath
     *            The path to the node.
     * @return Builds a node with
     *         {@link net.lab0.nebula.data.MandelbrotQuadTreeNode#positionToDepthAndBitSetPath(String)
     *         positionToDepthAndBitSetPath(String)}
     */
    public static MandelbrotQuadTreeNode buildNode(String stringPath)
    {
        return new MandelbrotQuadTreeNode(MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(stringPath));
    }
    
    /**
     * @param positions
     *            The path to the node.
     * @return Builds a node with
     *         net.lab0.nebula.data.MandelbrotQuadTreeNode.Factory#positionToDepthAndBitSetPath(PositionInParent...
     *         positions)
     */
    public static MandelbrotQuadTreeNode buildNode(PositionInParent... positions)
    {
        return new MandelbrotQuadTreeNode(MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(positions));
    }
    
    /**
     * @param parentPath
     *            The path of the parent node.
     * @param childPosition
     *            The position of this node in its parent
     * @return Builds a node with
     *         net.lab0.nebula.data.MandelbrotQuadTreeNode.Factory#positionToDepthAndBitSetPath(PositionInParent...
     *         positions) by concatenating the parent and child paths.
     */
    public static MandelbrotQuadTreeNode buildNode(PositionInParent[] parentPath, PositionInParent childPosition)
    {
        PositionInParent[] fullPath = Arrays.copyOf(parentPath, parentPath.length + 1);
        fullPath[parentPath.length] = childPosition;
        return new MandelbrotQuadTreeNode(MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(fullPath));
    }
    
    /**
     * @return Builds a root node. Equivalent to buildNode(PositionInParent.Root)
     */
    public static MandelbrotQuadTreeNode buildRoot()
    {
        return new MandelbrotQuadTreeNode(MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(PositionInParent.Root));
    }
    
    /**
     * The minimum number of iterations this node has (estimation). This value makes sense only when the status of the
     * node is {@link Status}<code>.OUTSIDE</code>.
     */
    public long     minimumIteration = -1;
    /**
     * The maximum number of iterations this node has (estimation). This value can be bound by the maximum iteration
     * value that was used during computation.
     */
    public long     maximumIteration = -1;
    public Status   status           = Status.VOID;
    public NodePath nodePath;
    
    /**
     * 
     * @param depth
     *            The depth of the node. <code>depth</code>>=0
     * @param path
     *            The path of the node. Not <code>null</code>.
     */
    protected MandelbrotQuadTreeNode(int depth, BitSet path)
    {
        this.nodePath = new NodePath(depth, BitSet.valueOf(path.toLongArray()));
    }
    
    /**
     * Same as <code>MandelbrotQuadTreeNode(path.getDepth(), path.getPath())</code>
     * 
     * @param path
     *            The path to the node
     */
    public MandelbrotQuadTreeNode(NodePath path)
    {
        // use that constructor to check the values and copy the BitSet
        this(path.getDepth(), path.getPath());
    }
    
    /**
     * Creates a fully initialized node.
     * 
     * @param depth
     *            >=0
     * @param path
     *            not null.
     * @param minimumIteration
     *            The minimum number of iterations in this node.
     * @param maximumIteration
     *            The maximum number of iterations in this node.
     * @param status
     *            The status of this node
     */
    public MandelbrotQuadTreeNode(int depth, BitSet path, long minimumIteration, long maximumIteration, Status status)
    {
        this.nodePath = new NodePath(depth, BitSet.valueOf(path.toLongArray()));
        this.minimumIteration = minimumIteration;
        this.maximumIteration = maximumIteration;
        this.status = status;
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
        for (int i = 1; i <= nodePath.depth; ++i)
        {
            // the current position in path is indicated by the bits depth*2 and depth*2+1 in path[]
            if (nodePath.path.get(2 * i + 1)) // at the left
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
        for (int i = 1; i <= nodePath.depth; ++i)
        {
            // the current position in path is indicated by the bits depth*2 and depth*2+1 in path[]
            if (nodePath.path.get(2 * i)) // at the bottom
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
    
    /**
     * Splits the current node into 4 sub-nodes.
     * 
     * @return An array containing the 4 sub-nodes.
     */
    public MandelbrotQuadTreeNode[] split()
    {
        MandelbrotQuadTreeNode[] splitted = new MandelbrotQuadTreeNode[4];
        PositionInParent[] positions = this.getPathAsEnum();
        splitted[0] = MandelbrotQuadTreeNode.buildNode(positions, PositionInParent.TopLeft);
        splitted[1] = MandelbrotQuadTreeNode.buildNode(positions, PositionInParent.TopRight);
        splitted[2] = MandelbrotQuadTreeNode.buildNode(positions, PositionInParent.BottomLeft);
        splitted[3] = MandelbrotQuadTreeNode.buildNode(positions, PositionInParent.BottomRight);
        return splitted;
    }
    
    /**
     * Converts the {@link String} to an array of {@link PositionInParent} to create the node's path.
     * 
     * @param stringPath
     *            The path to convert. Format must be R[1-4]*. Not check is made before the conversion.
     * @return A list of positions.
     */
    private static List<PositionInParent> convertStringToPath(String stringPath)
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
        return positions;
    }
    
    /**
     * Helper method to create a node from its String path.
     * 
     * @param stringPath
     *            The path to convert. Format must be <code>R[0-3]*</code>. Not check is made before the conversion.
     * @return A {@link NodePath} representing the given stringPath
     */
    public static NodePath positionToDepthAndBitSetPath(String stringPath)
    {
        List<PositionInParent> positions = convertStringToPath(stringPath);
        return positionToDepthAndBitSetPath(positions.toArray(new PositionInParent[positions.size()]));
    }
    
    /**
     * Creates a node from an array of {@link PositionInParent}.
     * 
     * @param positions
     *            The positions of the node in its parents, from root to itself. The ROOT position must be used exactly
     *            once: at the first position.
     * @return The {@link NodePath} equivalent to the given path.
     */
    public static NodePath positionToDepthAndBitSetPath(PositionInParent... positions)
    {
        BitSet path = new BitSet(positions.length * 2);
        int index = 0;
        if (positions.length == 0)
        {
            throw new IllegalArgumentException("The path must contain at least 1 element: the root");
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
    
    /**
     * 
     * @return The path converted to a series of {@link PositionInParent}
     */
    public PositionInParent[] getPathAsEnum()
    {
        PositionInParent[] positions = new PositionInParent[nodePath.depth + 1];
        positions[0] = PositionInParent.Root;
        for (int i = 1; i <= nodePath.depth; ++i)
        {
            int val = 0;
            if (nodePath.path.get(2 * i))
            {
                val += 2;
            }
            if (nodePath.path.get(2 * i + 1))
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
            }
        }
        return positions;
    }
    
    /**
     * 
     * @return The path converted to a {@link String} using the <code>R[0-3]*</code> format.
     */
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
            }
        }
        return sb.toString();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("depth=").append(nodePath.depth).append(" - path=" + getPathAsString());
        return sb.toString();
    }
}
