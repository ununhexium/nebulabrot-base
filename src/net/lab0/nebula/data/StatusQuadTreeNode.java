package net.lab0.nebula.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import net.lab0.nebula.enums.PositionInParent;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.exception.InconsistentTreeStructure;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * The nodes used for the computation of the quad tree. The root node has a depth of 0.
 * 
 * @author 116
 * 
 */
public class StatusQuadTreeNode
{
    /**
     * the link to the parent node. If the parent is null, then this is a quad tree root
     */
    public StatusQuadTreeNode   parent;
    
    /**
     * if children is not null, children must be QuadTreeNode[4]
     */
    public StatusQuadTreeNode[] children;
    
    /**
     * the status of this node. Must not be null.
     */
    public Status               status;
    
    /**
     * The minimum number of iterations. If negative : was not set. This means that it was never computed (and should then have the VOID status).
     */
    private int                 min               = -1;
    
    /**
     * The maximum number of iterations. If negative : was not set which means either that it was not computed or that it was over the iteration limit
     */
    private int                 max               = -1;
    
    private boolean             flagedForComputing;
    
    /**
     * The absolute path regex
     */
    private static Pattern      absolutePathRegex = Pattern.compile("R[0-3]*");
    /**
     * The relative path regex
     */
    private static Pattern      relativePathRegex = Pattern.compile("[0-3]+");
    
    /**
     * Creates an empty quad tree node
     */
    protected StatusQuadTreeNode()
    {
        // this would be a root node
        this.parent = null;
        this.status = Status.VOID;
    }
    
    /**
     * Creates a node with the given parameters. If parent is null, them this will be a root node.
     * 
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     * @param parent
     *            the parent node
     * @param positionInParent
     *            only used if parent is not null
     */
    public StatusQuadTreeNode(StatusQuadTreeNode parent)
    {
        this.parent = parent;
        this.children = null;
        this.status = Status.VOID;
    }
    
    /**
     * Creates a quad tree node from an xml element. If parent is null, the created node will be a root node.
     * 
     * @param nodeElement
     *            the xml node
     * @param parent
     *            the parent node
     */
    public StatusQuadTreeNode(Element nodeElement, StatusQuadTreeNode parent)
    {
        this.parent = parent;
        
        String statusString = nodeElement.getAttributeValue("status");
        this.status = Status.valueOf(statusString);
        
        if (this.status == Status.OUTSIDE)
        {
            this.min = Integer.parseInt(nodeElement.getAttributeValue("min"));
            this.max = Integer.parseInt(nodeElement.getAttributeValue("max"));
        }
        
        Elements childNodes = nodeElement.getChildElements("node");
        if (childNodes.size() > 0)
        {
            this.splitNode();
            for (int i = 0; i < childNodes.size(); ++i)
            {
                StatusQuadTreeNode childNode = new StatusQuadTreeNode(childNodes.get(i), this);
                String positionInParentString = childNodes.get(i).getAttributeValue("pos");
                PositionInParent positionInParent = PositionInParent.valueOf(positionInParentString);
                if (isChildNode(positionInParent))
                {
                    this.children[positionInParent.ordinal()] = childNode;
                }
            }
        }
    }
    
    /**
     * Splits this node. Creates 4 children, initialized with the appropriate fields' values and a VOID status
     */
    public void splitNode()
    {
        // split only if it's not already split
        if (this.children == null)
        {
            this.children = new StatusQuadTreeNode[4];
            
            for (int i = 0; i < 4; ++i)
            {
                children[i] = new StatusQuadTreeNode(this);
            }
        }
    }
    
    /**
     * Returns true if this is a child node position.
     * 
     * @param position
     * @return <code>true</code> if child node position. <code>false</code> if <code>this</code> is a root indicator which must be equivalent to say that
     *         <code>this</code> is a root node.
     */
    private boolean isChildNode(PositionInParent position)
    {
        return position != PositionInParent.Root;
    }
    
    private double getCenterY()
    {
        return getMinY() / 2.0d + getMaxY() / 2.0d;
    }
    
    private double getCenterX()
    {
        return getMinX() / 2.0d + getMaxX() / 2.0d;
    }
    
    /**
     * Assigns the {@link Status}.INSIDE value if the node is inside the mandelbrot set.
     * 
     * @param pointsPerSide
     * @param maxIter
     */
    private void testInsideMandelbrotSet(int pointsPerSide, int maxIter)
    {
        double minX = this.getMinX();
        double maxX = this.getMaxX();
        double minY = this.getMinY();
        double maxY = this.getMaxY();
        
        double step = (maxX - minX) / (double) (pointsPerSide - 1);
        
        // bottom side of the rectangle
        for (int i = 0; i < pointsPerSide; i++)
        {
            double real = (minX + (double) i * step);
            double img = minY;
            
            double realsqr = real * real;
            double imgsqr = img * img;
            
            double real1 = real;
            double img1 = img;
            double real2, img2;
            
            int iter = 0;
            while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
            {
                real2 = real1 * real1 - img1 * img1 + real;
                img2 = 2 * real1 * img1 + img;
                
                real1 = real2 * real2 - img2 * img2 + real;
                img1 = 2 * real2 * img2 + img;
                
                realsqr = real2 * real2;
                imgsqr = img2 * img2;
                real1 = realsqr - imgsqr + real;
                img1 = 2 * real2 * img2 + img;
                
                iter += 2;
            }
            
            if (iter < maxIter)
            {
                return;
            }
        }
        
        // top side of the rectangle
        for (int i = 0; i < pointsPerSide; ++i)
        {
            double real = (minX + (double) i * step);
            double img = maxY;
            
            double realsqr = real * real;
            double imgsqr = img * img;
            
            double real1 = real;
            double img1 = img;
            double real2, img2;
            
            int iter = 0;
            while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
            {
                real2 = real1 * real1 - img1 * img1 + real;
                img2 = 2 * real1 * img1 + img;
                
                real1 = real2 * real2 - img2 * img2 + real;
                img1 = 2 * real2 * img2 + img;
                
                realsqr = real2 * real2;
                imgsqr = img2 * img2;
                real1 = realsqr - imgsqr + real;
                img1 = 2 * real2 * img2 + img;
                
                iter += 2;
            }
            
            if (iter < maxIter)
            {
                return;
            }
        }
        
        // left side of the rectangle
        for (int i = 0; i < pointsPerSide; ++i)
        {
            double real = (minX);
            double img = (minY + (double) i * step);
            
            double realsqr = real * real;
            double imgsqr = img * img;
            
            double real1 = real;
            double img1 = img;
            double real2, img2;
            
            int iter = 0;
            while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
            {
                real2 = real1 * real1 - img1 * img1 + real;
                img2 = 2 * real1 * img1 + img;
                
                real1 = real2 * real2 - img2 * img2 + real;
                img1 = 2 * real2 * img2 + img;
                
                realsqr = real2 * real2;
                imgsqr = img2 * img2;
                real1 = realsqr - imgsqr + real;
                img1 = 2 * real2 * img2 + img;
                
                iter += 2;
            }
            
            if (iter < maxIter)
            {
                return;
            }
        }
        
        // bottom side of the rectangle
        for (int i = 0; i < pointsPerSide; ++i)
        {
            double real = (maxX);
            double img = (minY + (double) i * step);
            
            double realsqr = real * real;
            double imgsqr = img * img;
            
            double real1 = real;
            double img1 = img;
            double real2, img2;
            
            int iter = 0;
            while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
            {
                real2 = real1 * real1 - img1 * img1 + real;
                img2 = 2 * real1 * img1 + img;
                
                real1 = real2 * real2 - img2 * img2 + real;
                img1 = 2 * real2 * img2 + img;
                
                realsqr = real2 * real2;
                imgsqr = img2 * img2;
                real1 = realsqr - imgsqr + real;
                img1 = 2 * real2 * img2 + img;
                
                iter += 2;
            }
            
            if (iter < maxIter)
            {
                return;
            }
        }
        
        this.status = Status.INSIDE;
    }
    
    /**
     * Assigns the {@link Status}.OUTSIDE or BROWED status.
     * 
     * @param pointsPerSide
     * @param maxIter
     */
    private void testOutsideMandelbrotSet(int pointsPerSide, int maxIter, int diffIterLimit)
    {
        double minX = this.getMinX();
        double maxX = this.getMaxX();
        double minY = this.getMinY();
        double maxY = this.getMaxY();
        
        int min, max;
        // init min and max iter
        {
            double real = minX;
            double img = minY;
            double realsqr = real * real;
            double imgsqr = img * img;
            
            double real1 = real;
            double img1 = img;
            double real2, img2;
            
            int iter = 0;
            while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
            {
                real2 = real1 * real1 - img1 * img1 + real;
                img2 = 2 * real1 * img1 + img;
                
                real1 = real2 * real2 - img2 * img2 + real;
                img1 = 2 * real2 * img2 + img;
                
                realsqr = real2 * real2;
                imgsqr = img2 * img2;
                real1 = realsqr - imgsqr + real;
                img1 = 2 * real2 * img2 + img;
                
                iter += 2;
            }
            min = iter;
            max = iter;
        }
        
        double stepX = (maxX - minX) / (double) (pointsPerSide - 1);
        double stepY = (maxY - minY) / (double) (pointsPerSide - 1);
        
        // bottom side of the rectangle
        for (int i = 0; i < pointsPerSide; i++)
        {
            double real = minX + (double) i * stepX;
            for (int j = 0; j < pointsPerSide; ++j)
            {
                double img = (minY + (double) j * stepY);
                
                double realsqr = real * real;
                double imgsqr = img * img;
                
                double real1 = real;
                double img1 = img;
                double real2, img2;
                
                int iter = 0;
                while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
                {
                    real2 = real1 * real1 - img1 * img1 + real;
                    img2 = 2 * real1 * img1 + img;
                    
                    real1 = real2 * real2 - img2 * img2 + real;
                    img1 = 2 * real2 * img2 + img;
                    
                    realsqr = real2 * real2;
                    imgsqr = img2 * img2;
                    real1 = realsqr - imgsqr + real;
                    img1 = 2 * real2 * img2 + img;
                    
                    iter += 2;
                }
                
                if (iter < min)
                {
                    min = iter;
                }
                else if (iter > max)
                {
                    max = iter;
                }
                
                // System.out.println("" + real + "+i" + img + " iter=" + iter +
                // " diff=" + (max - min + 1));
                
                if ((max - min + 1) > diffIterLimit)
                {
                    this.status = Status.BROWSED;
                    return;
                }
            }
        }
        
        this.status = Status.OUTSIDE;
        this.min = min;
        this.max = max;
    }
    
    /**
     * Assigns the {@link Status} INSIDE, OUTSIDE, or BROWSED to this node
     * 
     * @param pointsPerSide
     * @param maxIter
     * @param diffIterLimit
     */
    public void computeStatus(int pointsPerSide, int maxIter, int diffIterLimit)
    {
        // System.out.println(Thread.currentThread().getName() + " computing testInsideMandelbrotSet");
        this.testInsideMandelbrotSet(pointsPerSide, maxIter);
        // System.out.println("After inside test " + this.status);
        if (this.status != Status.INSIDE)
        {
            // System.out.println(Thread.currentThread().getName() + " computing testOutsideMandelbrotSet");
            this.testOutsideMandelbrotSet(pointsPerSide, maxIter, diffIterLimit);
            // System.out.println("After outside test " + this.status);
        }
    }
    
    /**
     * 
     * @return the path of this node if the path in the tree to this node is valid. For instance : R0123.
     */
    public String getPath()
    {
        if (this.parent == null)
        {
            return "R";
        }
        else
        {
            return parent.getPath() + this.getPositionInParent().ordinal();
        }
    }
    
    /**
     * Converts this node to an xml {@link Element}
     * 
     * @param recursive
     *            if <code>true</code>, converts recursively all children
     * @return the xml {@link Element}
     * @throws InconsistentTreeStructure
     *             if there is an error in the quad tree
     */
    // TODO : check tree structure validity
    public Element asXML(boolean recursive)
    throws InconsistentTreeStructure
    {
        Element thisNode = new Element("node");
        thisNode.addAttribute(new Attribute("minX", "" + getMinX()));
        thisNode.addAttribute(new Attribute("maxX", "" + getMaxX()));
        thisNode.addAttribute(new Attribute("minY", "" + getMinY()));
        thisNode.addAttribute(new Attribute("maxY", "" + getMaxY()));
        
        thisNode.addAttribute(new Attribute("pos", getPositionInParent().toString()));
        thisNode.addAttribute(new Attribute("status", status.toString()));
        
        if (status == Status.OUTSIDE)
        {
            thisNode.addAttribute(new Attribute("min", "" + min));
            thisNode.addAttribute(new Attribute("max", "" + max));
        }
        
        if (recursive && children != null)
        {
            for (StatusQuadTreeNode node : children)
            {
                thisNode.appendChild(node.asXML(recursive));
            }
        }
        
        return thisNode;
    }
    
    /**
     * Computes the surface of this node
     * 
     * @return the surface of this node
     */
    public double getSurface()
    {
        double xDiff = getMaxX() - getMinX();
        double yDiff = getMaxY() - getMinY();
        return xDiff * yDiff;
    }
    
    /**
     * Get a node for the given path. This method should be (but is not required to be) called on the root of the tree. The node is searched in the whole tree
     * containing this node, starting by the root node and must therefore start with an R. For relative path, use {@link getSubnodeByPath}.
     * 
     * @param path
     *            The path of the node to get.
     * @return a {@link StatusQuadTreeNode} is any is found. <code>null</code> otherwise.
     * @throws IllegalArgumentException
     *             if the path is invalid
     */
    public StatusQuadTreeNode getNodeByAbsolutePath(String path)
    {
        if (!absolutePathRegex.matcher(path).matches())
        {
            throw new IllegalArgumentException("Invalid path " + path);
        }
        return getRootNode().getNodeByPathRecursively(path);
    }
    
    /**
     * Get a node for the given path. The node is searched from the node it was invoked on. The given path must be relative. For instance, to get
     * <code>this</code>, use <code>""</code>(empty string). To get the top left child, use <code>"0"</code>.
     * 
     * @param path
     *            The relative path to another node
     * @return The requested node if any, <code>null</code> otherwise.
     * @throws IllegalArgumentException
     *             if the path is invalid
     */
    public StatusQuadTreeNode getSubnodeByRelativePath(String path)
    {
        if (!relativePathRegex.matcher(path).matches())
        {
            throw new IllegalArgumentException("Invalid path " + path);
        }
        return getNodeByPathRecursively(Integer.toString(this.getPositionInParent().ordinal()) + path);
    }
    
    private StatusQuadTreeNode getNodeByPathRecursively(String path)
    {
        if (path.length() == 0)
        {
            throw new IllegalArgumentException("The path can't be an empty string.");
        }
        
        // pop 1st char : this node
        path = path.substring(1);
        
        // if no more char : we are the node which was seeked
        if (path.length() == 0)
        {
            return this;
        }
        
        // if we are not the last node in the path but we don't have any child :
        // can't find the node
        if (children == null)
        {
            return null;
        }
        
        // find the node to go to
        switch (path.charAt(0))
        {
            case '0':
                return children[0].getNodeByPathRecursively(path);
                
            case '1':
                return children[1].getNodeByPathRecursively(path);
                
            case '2':
                return children[2].getNodeByPathRecursively(path);
                
            case '3':
                return children[3].getNodeByPathRecursively(path);
                
            default:
                assert (false);
                break;
        }
        
        return null;
    }
    
    /**
     * 
     * @return the root of the tree containing this node
     */
    private StatusQuadTreeNode getRootNode()
    {
        if (parent == null)
        {
            return this;
        }
        else
        {
            return this.parent.getRootNode();
        }
    }
    
    // /**
    // * ensures the existence of the children array but not its content
    // */
    // public void ensureChildrenArray()
    // {
    // if (children == null)
    // {
    // children = new QuadTreeNode[4];
    // }
    // }
    
    /**
     * Returns the nodes having a {@link Status} in <code>status</code> and puts it in <code>nodesList</code>
     * 
     * @param nodesList
     *            a list which will contain the nodes
     * @param status
     *            the status the nodes must have to be returned
     */
    public void getNodesByStatus(List<StatusQuadTreeNode> nodesList, List<Status> status)
    {
        if (status.contains(this.status))
        {
            nodesList.add(this);
        }
        
        if (this.children != null)
        {
            for (StatusQuadTreeNode child : children)
            {
                child.getNodesByStatus(nodesList, status);
            }
        }
    }
    
    /**
     * Returns the nodes having a {@link Status} in <code>status</code> and puts it in <code>nodesList</code>
     * 
     * @param nodesList
     *            a list which will contain the nodes
     * @param status
     *            the status the nodes must have to be returned
     * @param maxQuantity
     *            the method stops when the list has at least <code>maxQuantity</code> elements in it.
     */
    public void getNodesByStatus(List<StatusQuadTreeNode> nodesList, List<Status> status, int maxQuantity)
    {
        if (status.contains(this.status))
        {
            nodesList.add(this);
        }
        
        if (nodesList.size() >= maxQuantity)
        {
            return;
        }
        
        if (this.children != null)
        {
            for (StatusQuadTreeNode child : children)
            {
                child.getNodesByStatus(nodesList, status, maxQuantity);
            }
        }
    }
    
    /**
     * 
     * @return <code>true</code> if the node has computed direct children
     */
    public boolean hasComputedChildren()
    {
        if (this.status == Status.VOID)
        {
            return false;
        }
        
        if (children == null)
        {
            return false;
        }
        
        for (StatusQuadTreeNode child : children)
        {
            if (child.status != Status.VOID)
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Flags this node to inform that it is being computed.
     */
    public synchronized void flagForComputing()
    {
        if (!flagedForComputing)
        {
            flagedForComputing = true;
        }
    }
    
    /**
     * Removes the falg indicating that the node is being computed.
     */
    public synchronized void unFlagForComputing()
    {
        if (flagedForComputing)
        {
            flagedForComputing = false;
        }
    }
    
    public boolean isFlagedForComputing()
    {
        return flagedForComputing;
    }
    
    /**
     * Retrieves the leaf nodes of this branch.
     * 
     * @param leafNodes
     *            a list which will contain the leaf nodes.
     */
    public void getLeafNodes(List<StatusQuadTreeNode> leafNodes)
    {
        if (this.children == null)
        {
            leafNodes.add(this);
        }
        else
        {
            for (StatusQuadTreeNode child : children)
            {
                child.getLeafNodes(leafNodes);
            }
        }
    }
    
    /**
     * Retrieves the leaf nodes of this branch.
     * 
     * @param leafNodes
     *            a list which will contain the leaf nodes.
     * @param status
     *            the node must have one of the given status to be retrieved
     */
    public void getLeafNodes(List<StatusQuadTreeNode> leafNodes, List<Status> status)
    {
        if (this.children == null)
        {
            if (status.contains(this.status))
            {
                leafNodes.add(this);
            }
        }
        else
        {
            for (StatusQuadTreeNode child : children)
            {
                child.getLeafNodes(leafNodes, status);
            }
        }
    }
    
    /**
     * Retrieves the leaf nodes of this branch. They are retrieved only if they are usable for the given min/max iter interval.
     * 
     * @param leafNodes
     *            a list which will contain the leaf nodes.
     * @param status
     *            the node must have one of the given status to be retrieved
     * @param minIter
     * @param maxIter
     */
    public void getLeafNodes(List<StatusQuadTreeNode> leafNodes, List<Status> status, int minIter, int maxIter)
    {
        if (this.children == null)
        {
            if (status.contains(this.status))
            {
                if (this.status == Status.OUTSIDE)
                {
                    if (!(this.max < minIter || this.min > maxIter))
                    {
                        leafNodes.add(this);
                    }
                }
                else
                {
                    leafNodes.add(this);
                }
            }
        }
        else
        {
            for (StatusQuadTreeNode child : children)
            {
                child.getLeafNodes(leafNodes, status, minIter, maxIter);
            }
        }
    }
    
    /**
     * 
     * @return The depth of the deepest node in this node subtree.
     */
    public int getMaxNodeDepth()
    {
        if (children == null)
        {
            return this.getDepth();
        }
        else
        {
            return Math.max(Math.max(children[0].getMaxNodeDepth(), children[1].getMaxNodeDepth()),
            Math.max(children[2].getMaxNodeDepth(), children[3].getMaxNodeDepth()));
        }
    }
    
    /**
     * 
     * @param p1
     * @param p2
     * @return The nodes overlapping the given closed rectangle.
     */
    public Collection<StatusQuadTreeNode> getNodesOverlappingRectangle(Point2D.Double p1, Point2D.Double p2)
    {
        ArrayList<StatusQuadTreeNode> c = new ArrayList<StatusQuadTreeNode>();
        getNodesOverlappingRectangle(p1, p2, c);
        return c;
    }
    
    /**
     * Adds all nodes and subnodes contained in the rectangle defined by the 2 given points in <code>collection</code>
     * 
     * @param rectMaxX
     * @param rectMaxY
     * @param rectMinX
     * @param rectMinY
     * @param collection
     *            la collection contenant le rﾃｩsultat
     */
    public void getNodesOverlappingRectangle(Point2D.Double p1, Point2D.Double p2, Collection<StatusQuadTreeNode> collection)
    {
        double rectMaxX = Math.max(p1.getX(), p2.getX());
        double rectMaxY = Math.max(p1.getY(), p2.getY());
        double rectMinX = Math.min(p1.getX(), p2.getX());
        double rectMinY = Math.min(p1.getY(), p2.getY());
        
        getNodesOverlappingRectangle(rectMaxX, rectMaxY, rectMinX, rectMinY, collection);
    }
    
    /**
     * Adds all nodes and subnodes contained in the rectangle defined by its 4 edges in <code>collection</code>
     * 
     * @param rectMaxX
     * @param rectMaxY
     * @param rectMinX
     * @param rectMinY
     * @param collection
     *            la collection contenant le rﾃｩsultat
     */
    private void getNodesOverlappingRectangle(double rectMaxX, double rectMaxY, double rectMinX, double rectMinY, Collection<StatusQuadTreeNode> collection)
    {
        // si la zone de cette node est entiﾃｨrement contenue dans le rectangle
        if (this.getMinX() >= rectMinX && this.getMaxX() <= rectMaxX && this.getMaxY() <= rectMaxY && this.getMinY() >= rectMinY)
        {
            this.getAllNodes(collection);
        }
        else
        {
            // si la zone de cette node est partiellement contenue dans le rectangle
            if ((this.getMinX() <= rectMaxX || this.getMaxX() >= rectMinX) && (this.getMinY() <= rectMaxY || this.getMaxY() >= rectMinY))
            {
                collection.add(this);
            }
            // si la node a des enfants : tester lesquels sont dans le rectangle
            if (children != null)
            {
                for (PositionInParent zone : getZonesOverlappingRectangle(rectMaxX, rectMaxY, rectMinX, rectMinY))
                {
                    children[zone.ordinal()].getNodesOverlappingRectangle(rectMaxX, rectMaxY, rectMinX, rectMinY, collection);
                }
            }
        }
    }
    
    public void getAllNodes(Collection<StatusQuadTreeNode> collection)
    {
        collection.add(this);
        if (children != null)
        {
            for (StatusQuadTreeNode n : children)
            {
                n.getAllNodes(collection);
            }
        }
    }
    
    /**
     * Returns the areas of this zone which touch the rectangles defined by its 4 edges.
     * 
     * @param rectMaxX
     * @param rectMaxY
     * @param rectMinX
     * @param rectMinY
     * @return a list of the zones contained in the rectangle
     */
    protected ArrayList<PositionInParent> getZonesOverlappingRectangle(double rectMaxX, double rectMaxY, double rectMinX, double rectMinY)
    {
        ArrayList<PositionInParent> ret = new ArrayList<PositionInParent>(4);
        
        if (getCenterX() <= rectMinX) // le split est ﾃ�gauche du rectangle
        {
            if (getCenterY() <= rectMinY)
            {
                ret.add(PositionInParent.TopRight);
            }
            else if (getCenterY() > rectMaxY)
            {
                ret.add(PositionInParent.BottomRight);
            }
            else
            {
                ret.add(PositionInParent.TopRight);
                ret.add(PositionInParent.BottomRight);
            }
        }
        else if (getCenterX() > rectMaxX) // le split est ﾃ�droite du rectangle
        {
            if (getCenterY() <= rectMinY)
            {
                ret.add(PositionInParent.TopLeft);
            }
            else if (getCenterY() > rectMaxY)
            {
                ret.add(PositionInParent.BottomLeft);
            }
            else
            {
                ret.add(PositionInParent.TopLeft);
                ret.add(PositionInParent.BottomLeft);
            }
        }
        else
        // le split est au dans le rectangle en x
        {
            if (getCenterY() <= rectMinY)
            {
                ret.add(PositionInParent.TopLeft);
                ret.add(PositionInParent.TopRight);
            }
            else if (getCenterY() > rectMaxY)
            {
                ret.add(PositionInParent.BottomLeft);
                ret.add(PositionInParent.BottomRight);
            }
            else
            {
                ret.add(PositionInParent.TopLeft);
                ret.add(PositionInParent.TopRight);
                ret.add(PositionInParent.BottomLeft);
                ret.add(PositionInParent.BottomRight);
            }
        }
        
        return ret;
    }
    
    public boolean isLeafNode()
    {
        return children == null;
    }
    
    @Override
    public String toString()
    {
        return "AbstractQuadTreeNode [minX=" + getMinX() + ", maxX=" + getMaxX() + ", minY=" + getMinY() + ", maxY=" + getMaxY() + ", status=" + status + "]";
    }
    
    /**
     * test that all fields are exactly the same. Doesn't test parents.
     * 
     * @param other
     * @return
     */
    public boolean testIsExactlyTheSameAs(StatusQuadTreeNode other, boolean testFlag)
    {
        if (testFlag)
        {
            if (!this.flagedForComputing == other.flagedForComputing)
            {
                System.out.println("flag mismatch");
                System.out.println("this" + this.completeToString());
                System.out.println("other" + other.completeToString());
                return false;
            }
        }
        
        if (this.getMinX() != other.getMinX())
        {
            System.out.println("minX mismatch");
            System.out.println("this" + this.completeToString());
            System.out.println("other" + other.completeToString());
            return false;
        }
        
        if (this.getMaxX() != other.getMaxX())
        {
            System.out.println("maxX mismatch");
            System.out.println("this" + this.completeToString());
            System.out.println("other" + other.completeToString());
            return false;
        }
        
        if (this.getMinY() != other.getMinY())
        {
            System.out.println("minY mismatch");
            System.out.println("this" + this.completeToString());
            System.out.println("other" + other.completeToString());
            return false;
        }
        
        if (this.getMaxY() != other.getMaxY())
        {
            System.out.println("maxY mismatch");
            System.out.println("this" + this.completeToString());
            System.out.println("other" + other.completeToString());
            return false;
        }
        if (this.getDepth() != other.getDepth())
        {
            System.out.println("depth mismatch");
            System.out.println("this" + this.completeToString());
            System.out.println("other" + other.completeToString());
            return false;
        }
        
        if (this.getPositionInParent() != other.getPositionInParent())
        {
            System.out.println("position in parent mismatch");
            System.out.println("this" + this.completeToString());
            System.out.println("other" + other.completeToString());
            return false;
        }
        
        if (this.status != other.status)
        {
            System.out.println("status mismatch");
            System.out.println("this" + this.completeToString());
            System.out.println("other" + other.completeToString());
            return false;
        }
        
        if (!(this.getMin() == other.getMin() && this.getMax() == other.getMax()))
        {
            System.out.println("min/max mismatch");
            System.out.println("this" + this.completeToString());
            System.out.println("other" + other.completeToString());
            return false;
        }
        
        if (this.children == null)
        {
            if (other.children == null)
            {
                return true;
            }
            else
            {
                System.out.println("children : null / not null");
                System.out.println("this" + this.completeToString());
                System.out.println("other" + other.completeToString());
                return false;
            }
        }
        else
        {
            boolean same = true;
            for (int i = 0; i < 4; ++i)
            {
                same &= this.children[i].testIsExactlyTheSameAs(other.children[i], testFlag);
                
                // stop as soon as there is a difference
                if (!same)
                {
                    System.out.println("children mismatch");
                    System.out.println("this" + this.completeToString());
                    System.out.println("other" + other.completeToString());
                    return false;
                }
            }
            return same;
        }
    }
    
    public String completeToString()
    {
        return "QuadTreeNode [parent=" + parent + ", children=" + Arrays.toString(children) + ", minX=" + getMinX() + ", maxX=" + getMaxX() + ", minY="
        + getMinY() + ", maxY=" + getMaxY() + ", depth=" + getDepth() + ", positionInParent=" + getPositionInParent() + ", status=" + status + ", min=" + min
        + ", max=" + max + ", flagedForComputing=" + flagedForComputing + "]";
    }
    
    /**
     * 
     * @return the total amount of nodes that this node contains. Includes itself and all its children.
     */
    public int getTotalNodesCount()
    {
        int total = 0;
        if (children == null)
        {
            total += 1;
        }
        else
        {
            for (StatusQuadTreeNode child : children)
            {
                total += child.getTotalNodesCount();
            }
        }
        return total;
    }
    
    /**
     * Removes any node deeper than <code>maxDepth</code>. A node of depth equal to <code>maxDepth</code> is kept.
     * 
     * @param maxDepth
     */
    public void strip(int maxDepth)
    {
        if (this.getDepth() > maxDepth)
        {
            throw new IllegalArgumentException("Trying to strip from a node of depth > maxDepth. Impossible operation.");
        }
        else if (this.getDepth() == maxDepth)
        {
            this.children = null;
        }
        else if (children != null)
        {
            for (StatusQuadTreeNode node : children)
            {
                node.strip(maxDepth);
            }
        }
    }
    
    /**
     * Recursively computes this node depth. The depth of a root node is 0.
     * 
     * This method was marked private because it consumes a lot of computation time. Use as few as possible.
     * 
     * @return the depth of this node.
     */
    public int getDepth()
    {
        if (parent == null)
        {
            return 0;
        }
        else
        {
            return 1 + parent.getDepth();
        }
    }
    
    public double getMinX()
    {
        switch (this.getPositionInParent())
        {
            case TopLeft:
            case BottomLeft:
                return this.parent.getMinX();
                
            case BottomRight:
            case TopRight:
                return this.parent.getCenterX();
                
            default:
                throw new InconsistentTreeStructure("The node has a parent but the position in parent is not defined.");
        }
    }
    
    public double getMaxX()
    {
        switch (this.getPositionInParent())
        {
            case TopLeft:
            case BottomLeft:
                return this.parent.getCenterX();
                
            case BottomRight:
            case TopRight:
                return this.parent.getMaxX();
                
            default:
                throw new InconsistentTreeStructure("The node has a parent but the position in parent is not defined.");
        }
    }
    
    public double getMinY()
    {
        switch (this.getPositionInParent())
        {
            case TopLeft:
            case TopRight:
                return this.parent.getCenterY();
                
            case BottomLeft:
            case BottomRight:
                return this.parent.getMinY();
                
            default:
                throw new InconsistentTreeStructure("The node has a parent but the position in parent is not defined.");
        }
    }
    
    public double getMaxY()
    {
        switch (this.getPositionInParent())
        {
            case TopLeft:
            case TopRight:
                return this.parent.getMaxY();
                
            case BottomLeft:
            case BottomRight:
                return this.parent.getCenterY();
                
            default:
                throw new InconsistentTreeStructure("The node has a parent but the position in parent is not defined.");
        }
    }
    
    public int getMin()
    {
        if (this.status == Status.OUTSIDE)
        {
            return min;
        }
        else
        {
            if (children != null)
            {
                return Math.min(Math.min(children[0].getMin(), children[1].getMin()), Math.min(children[2].getMin(), children[3].getMin()));
            }
            else
            {
                return -1;
            }
        }
    }
    
    public void setMin(int min)
    {
        this.min = min;
    }
    
    public int getMax()
    {
        return max;
    }
    
    public void setMax(int max)
    {
        this.max = max;
    }
    
    /**
     * TopLeft = 0, TopRight = 1, BottomLeft = 2, BottomRight = 3, Root = undef
     * 
     * @return the position of this node in the parent node.
     * 
     */
    public PositionInParent getPositionInParent()
    throws InconsistentTreeStructure
    {
        if (parent == null)
        {
            return PositionInParent.Root;
        }
        else
        {
            for (int i = 0; i < 4; ++i)
            {
                if (parent.children[i] == this)
                {
                    return PositionInParent.values()[i];
                }
            }
        }
        
        throw new InconsistentTreeStructure("The node has a parent but the parent doesn't contain this node (" + this + ")");
    }
    
    /**
     * updates the transient fields. Should be use at loading by the QuadTreeManager.
     */
    public void updateFields()
    {
        // will be set by the parent
        // this.parent;
        
        if (this.children != null)
        {
            for (int i = 0; i < 4; ++i)
            {
                this.children[i].parent = this;
                
                this.children[i].updateFields();
            }
        }
        
        // useless to set value
        // this.flagedForComputing = false;
    }
    
}
