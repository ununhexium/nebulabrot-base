package net.lab0.nebula.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.lab0.nebula.enums.PositionInParent;
import net.lab0.nebula.enums.Status;
import net.lab0.tools.geom.PointInterface;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * 
 * @author 116
 * 
 *         The root node has a depth of 0
 * 
 */
public class QuadTreeNode
{
    public QuadTreeNode     parent;
    public QuadTreeNode[]   children;
    
    public double           minX, maxX, minY, maxY;
    public int              depth;
    
    public PositionInParent positionInParent;
    public Status           status;
    public int              min = -1;
    public int              max = -1;
    
    private boolean         flagedForComputing;
    
    public QuadTreeNode(double minX, double maxX, double minY, double maxY)
    {
        this(minX, maxX, minY, maxY, null, PositionInParent.Root);
    }
    
    public QuadTreeNode(double minX, double maxX, double minY, double maxY, QuadTreeNode parent, PositionInParent positionInParent)
    {
        if (parent != null)
        {
            this.positionInParent = positionInParent;
            this.parent = parent;
            this.depth = parent.depth + 1;
        }
        else
        {
            this.positionInParent = PositionInParent.Root;
            this.depth = 0;
            this.parent = null;
        }
        
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
        
        this.children = null;
        
        this.status = Status.VOID;
    }
    
    public QuadTreeNode(Element nodeElement, QuadTreeNode parent)
    {
        this.parent = parent;
        if (parent == null)
        {
            this.positionInParent = PositionInParent.Root;
        }
        
        this.minX = Double.parseDouble(nodeElement.getAttributeValue("minX"));
        this.maxX = Double.parseDouble(nodeElement.getAttributeValue("maxX"));
        this.minY = Double.parseDouble(nodeElement.getAttributeValue("minY"));
        this.maxY = Double.parseDouble(nodeElement.getAttributeValue("maxY"));
        
        String positionInParentString = nodeElement.getAttributeValue("pos");
        
        switch (positionInParentString)
        {
            case "TopLeft":
                this.positionInParent = PositionInParent.TopLeft;
                break;
            
            case "TopRight":
                this.positionInParent = PositionInParent.TopRight;
                break;
            
            case "BottomLeft":
                this.positionInParent = PositionInParent.BottomLeft;
                break;
            
            case "BottomRight":
                this.positionInParent = PositionInParent.BottomRight;
                break;
        }
        
        String statusString = nodeElement.getAttributeValue("status");
        
        switch (statusString)
        {
            case "BROWSED":
                this.status = Status.BROWSED;
                break;
            
            case "OUTSIDE":
                this.status = Status.OUTSIDE;
                break;
            
            case "INSIDE":
                this.status = Status.INSIDE;
                break;
            
            case "VOID":
                this.status = Status.VOID;
                break;
        }
        
        if (this.status == Status.BROWSED)
        {
            this.min = Integer.parseInt(nodeElement.getAttributeValue("min"));
        }
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
                QuadTreeNode childNode = new QuadTreeNode(childNodes.get(i), this);
                if (isChildNode(childNode.positionInParent))
                {
                    this.children[childNode.positionInParent.ordinal()] = childNode;
                }
            }
        }
    }
    
    public void updateDepth()
    {
        this.depth = this.computeDepth();
        
        if (children != null)
        {
            for (int i = 0; i < 4; ++i)
            {
                children[i].updateDepth();
            }
        }
    }
    
    private int computeDepth()
    {
        if (parent == null)
        {
            return 0;
        }
        else
        {
            return 1 + parent.computeDepth();
        }
    }
    
    public void splitNode()
    {
        // split only if it's not already split
        if (this.children == null)
        {
            this.children = new QuadTreeNode[4];
            
            QuadTreeNode[] children = this.children;
            
            for (PositionInParent position : PositionInParent.values())
            {
                if (isChildNode(position))
                {
                    double minX = 0;
                    double maxX = 0;
                    double minY = 0;
                    double maxY = 0;
                    
                    switch (position)
                    {
                        case TopLeft:
                            minX = this.minX;
                            maxX = this.getCenterX();
                            minY = this.getCenterY();
                            maxY = this.maxY;
                            break;
                        
                        case TopRight:
                            
                            minX = this.getCenterX();
                            maxX = this.maxX;
                            minY = this.getCenterY();
                            maxY = this.maxY;
                            break;
                        
                        case BottomLeft:
                            minX = this.minX;
                            maxX = this.getCenterX();
                            minY = this.minY;
                            maxY = this.getCenterY();
                            break;
                        
                        case BottomRight:
                            
                            minX = this.getCenterX();
                            maxX = this.maxX;
                            minY = this.minY;
                            maxY = this.getCenterY();
                            break;
                        
                        default:
                            break;
                    }
                    
                    children[position.ordinal()] = new QuadTreeNode(minX, maxX, minY, maxY, this, position);
                }
            }
        }
    }
    
    private boolean isChildNode(PositionInParent position)
    {
        return position.equals(PositionInParent.TopLeft) || position.equals(PositionInParent.TopRight) || position.equals(PositionInParent.BottomLeft)
        || position.equals(PositionInParent.BottomRight);
    }
    
    private double getCenterY()
    {
        return (minY + maxY) / 2.0d;
    }
    
    private double getCenterX()
    {
        return (minX + maxX) / 2.0d;
    }
    
    public void testInsideMandelbrotSet(int pointsPerSide, int maxIter)
    {
        double minX = this.minX;
        double maxX = this.maxX;
        double minY = this.minY;
        double maxY = this.maxY;
        
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
    
    // private double[] borderPointsAsDouble(int pointsPerSide)
    // {
    // double minX = this.minX;
    // double maxX = this.maxX;
    // double minY = this.minY;
    // double maxY = this.maxY;
    //
    // double[] destArray = new double[pointsPerSide * 8];
    //
    // double step = (maxX - minX) / (double) (pointsPerSide - 1);
    //
    // // bottom side of the rectangle
    // int baseIndex = 0;
    // for (int i = 0; i < pointsPerSide; i++)
    // {
    // destArray[baseIndex + 2 * i] = (minX + (double) i * step);
    // destArray[baseIndex + 2 * i + 1] = (minY);
    // }
    // baseIndex += 2 * pointsPerSide;
    //
    // // top side of the rectangle
    // for (int i = 0; i < pointsPerSide; ++i)
    // {
    // destArray[baseIndex + 2 * i] = (minX + (double) i * step);
    // destArray[baseIndex + 2 * i + 1] = (maxY);
    // }
    // baseIndex += 2 * pointsPerSide;
    //
    // // left side of the rectangle
    // for (int i = 0; i < pointsPerSide; ++i)
    // {
    // destArray[baseIndex + 2 * i] = (minX);
    // destArray[baseIndex + 2 * i + 1] = (minY + (double) i * step);
    // }
    // baseIndex += 2 * pointsPerSide;
    //
    // // bottom side of the rectangle
    // for (int i = 0; i < pointsPerSide; ++i)
    // {
    // destArray[baseIndex + 2 * i] = (maxX);
    // destArray[baseIndex + 2 * i + 1] = (minY + (double) i * step);
    // }
    //
    // return destArray;
    // }
    
    public void testOutsideMandelbrotSet(int pointsPerSide, int maxIter, int diffIterLimit)
    {
        // double[] array = innerPointsAsDouble(pointsPerSide);
        
        double minX = this.minX;
        double maxX = this.maxX;
        double minY = this.minY;
        double maxY = this.maxY;
        
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
                    this.min = min;
                    return;
                }
            }
        }
        
        this.status = Status.OUTSIDE;
        this.min = min;
        this.max = max;
    }
    
    // private double[] innerPointsAsDouble(int pointsPerSide)
    // {
    // double minX = this.minX;
    // double maxX = this.maxX;
    // double minY = this.minY;
    // double maxY = this.maxY;
    //
    // double stepX = (maxX - minX) / (double) (pointsPerSide - 1);
    // double stepY = (maxY - minY) / (double) (pointsPerSide - 1);
    //
    // double[] array = new double[pointsPerSide * pointsPerSide * 2];
    //
    // // bottom side of the rectangle
    // for (int i = 0; i < pointsPerSide; i++)
    // {
    // int base = i * pointsPerSide;
    // double xVal = minX + (double) i * stepX;
    // for (int j = 0; j < pointsPerSide; ++j)
    // {
    // array[2 * (base + j)] = xVal;
    // array[2 * (base + j) + 1] = (minY + (double) j * stepY);
    // }
    // }
    //
    // return array;
    // }
    
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
    
    public String getPath()
    {
        if (this.parent == null)
        {
            return "R";
        }
        else
        {
            return parent.getPath() + this.positionInParent.ordinal();
        }
    }
    
    public Element asXML(boolean recursive)
    {
        Element thisNode = new Element("node");
        thisNode.addAttribute(new Attribute("minX", "" + minX));
        thisNode.addAttribute(new Attribute("maxX", "" + maxX));
        thisNode.addAttribute(new Attribute("minY", "" + minY));
        thisNode.addAttribute(new Attribute("maxY", "" + maxY));
        
        thisNode.addAttribute(new Attribute("pos", positionInParent.toString()));
        thisNode.addAttribute(new Attribute("status", status.toString()));
        
        // TODO : rm debug
        // thisNode.addAttribute(new Attribute("path", getPath()));
        
        if (status == Status.BROWSED)
        {
            thisNode.addAttribute(new Attribute("min", "" + min));
        }
        if (status == Status.OUTSIDE)
        {
            thisNode.addAttribute(new Attribute("min", "" + min));
            thisNode.addAttribute(new Attribute("max", "" + max));
        }
        
        if (recursive && children != null)
        {
            for (QuadTreeNode node : children)
            {
                thisNode.appendChild(node.asXML(recursive));
            }
        }
        
        return thisNode;
    }
    
    public double getSurface()
    {
        double xDiff = maxX - minX;
        double yDiff = maxY - minY;
        return xDiff * yDiff;
    }
    
    public QuadTreeNode getNodeByPath(String path)
    {
        if (parent == null)
        {
            return getNodeByPathRecusively(path);
        }
        else
        {
            // System.out.println("Seek parent");
            return getRootNode().getNodeByPath(path);
        }
    }
    
    private QuadTreeNode getNodeByPathRecusively(String path)
    {
        // System.out.println("Path = " + path);
        // pop 1st char : this node
        path = path.replaceFirst("[R0-3]", "");
        
        // if no more char : we are the node shich was seeked
        if (path.length() == 0)
        {
            return this;
        }
        
        // if we are not the last node in the path but we don't have any child :
        // can't find the node
        if (children == null)
        {
            System.out.println("no children");
            return null;
        }
        
        // find the node to go to
        switch (path.charAt(0))
        {
            case '0':
                return children[0].getNodeByPathRecusively(path);
                
            case '1':
                return children[1].getNodeByPathRecusively(path);
                
            case '2':
                return children[2].getNodeByPathRecusively(path);
                
            case '3':
                return children[3].getNodeByPathRecusively(path);
                
            default:
                assert (false);
                break;
        }
        
        return null;
    }
    
    private QuadTreeNode getRootNode()
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
    
    public void ensureChildrenArray()
    {
        if (children == null)
        {
            children = new QuadTreeNode[4];
        }
    }
    
    public void getNodesByStatus(List<QuadTreeNode> nodesList, List<Status> status)
    {
        if (status.contains(this.status))
        {
            nodesList.add(this);
        }
        
        if (this.children != null)
        {
            for (QuadTreeNode child : children)
            {
                child.getNodesByStatus(nodesList, status);
            }
        }
    }
    
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
        
        for (QuadTreeNode child : children)
        {
            if (child.status != Status.VOID)
            {
                return true;
            }
        }
        
        return false;
    }
    
    public synchronized void flagForComputing()
    {
        if (!flagedForComputing)
        {
            flagedForComputing = true;
        }
    }
    
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
    
    public int getMaxChildrenDepth()
    {
        if (children == null)
        {
            return depth;
        }
        else
        {
            return Math.max(Math.max(children[0].getMaxChildrenDepth(), children[1].getMaxChildrenDepth()),
            Math.max(children[2].getMaxChildrenDepth(), children[3].getMaxChildrenDepth()));
        }
    }
    
    public void getLeafNodes(List<QuadTreeNode> leafNodes)
    {
        if (this.children == null)
        {
            leafNodes.add(this);
        }
        else
        {
            for (QuadTreeNode child : children)
            {
                child.getLeafNodes(leafNodes);
            }
        }
    }
    
    public void getLeafNodes(List<QuadTreeNode> leafNodes, List<Status> status)
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
            for (QuadTreeNode child : children)
            {
                child.getLeafNodes(leafNodes, status);
            }
        }
    }
    
    public int getMaxNodeDepth()
    {
        if (children == null)
        {
            return this.depth;
        }
        else
        {
            return Math.max(Math.max(children[0].getMaxNodeDepth(), children[1].getMaxNodeDepth()),
            Math.max(children[2].getMaxNodeDepth(), children[3].getMaxNodeDepth()));
        }
    }
    
    public Collection<QuadTreeNode> getNodesInRectangle(Point2D.Double p1, Point2D.Double p2)
    {
        ArrayList<QuadTreeNode> c = new ArrayList<QuadTreeNode>();
        getNodesOverlappingRectangle(p1, p2, c);
        return c;
    }
    
    /**
     * Ajoute toutes les nodes et sous-nodes contenues dans le rectangle défini par les 2 points à <code>collection</code>
     * 
     * @param rectMaxX
     * @param rectMaxY
     * @param rectMinX
     * @param rectMinY
     * @param collection
     *            la collection contenant le résultat
     */
    public void getNodesOverlappingRectangle(Point2D.Double p1, Point2D.Double p2, Collection<QuadTreeNode> collection)
    {
        double rectMaxX = Math.max(p1.getX(), p2.getX());
        double rectMaxY = Math.max(p1.getY(), p2.getY());
        double rectMinX = Math.min(p1.getX(), p2.getX());
        double rectMinY = Math.min(p1.getY(), p2.getY());
        
        getNodesOverlappingRectangle(rectMaxX, rectMaxY, rectMinX, rectMinY, collection);
    }
    
    /**
     * Ajoute toutes les nodes et sous-nodes contenues dans le rectangle défini par ses 4 bords à <code>collection</code>
     * 
     * @param rectMaxX
     * @param rectMaxY
     * @param rectMinX
     * @param rectMinY
     * @param collection
     *            la collection contenant le résultat
     */
    private void getNodesOverlappingRectangle(double rectMaxX, double rectMaxY, double rectMinX, double rectMinY, Collection<QuadTreeNode> collection)
    {
        // si la zone de cette node est entièrement contenue dans le rectangle
        if (this.minX >= rectMinX && this.maxX <= rectMaxX && this.maxY <= rectMaxY && this.minY >= rectMinY)
        {
            this.getAllNodes(collection);
        }
        else
        {
            // si la zone de cette node est partiellement contenue dans le rectangle
            if ((this.minX <= maxX || this.maxX >= minX) && (this.minY <= maxY || this.maxY >= minY))
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
    
    private void getAllNodes(Collection<QuadTreeNode> collection)
    {
        collection.add(this);
        if (children != null)
        {
            for (QuadTreeNode n : children)
            {
                n.getAllNodes(collection);
            }
        }
    }
    
    /**
     * Renvoie les zones de cette node qui touchent le rectangle défini par ses 4 bords
     * 
     * @param rectMaxX
     * @param rectMaxY
     * @param rectMinX
     * @param rectMinY
     * @return une liste des zones contenues dans le rectangle
     */
    protected ArrayList<PositionInParent> getZonesOverlappingRectangle(double rectMaxX, double rectMaxY, double rectMinX, double rectMinY)
    {
        ArrayList<PositionInParent> ret = new ArrayList<PositionInParent>(4);
        
        if (getCenterX() <= rectMinX) // le split est à gauche du rectangle
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
        else if (getCenterX() > rectMaxX) // le split est à droite du rectangle
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
        return "QuadTreeNode [minX=" + minX + ", maxX=" + maxX + ", minY=" + minY + ", maxY=" + maxY + ", status=" + status + "]";
    }
    
    
}
