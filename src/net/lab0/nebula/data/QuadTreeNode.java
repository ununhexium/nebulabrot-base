package net.lab0.nebula.data;

import java.util.Date;
import java.util.List;

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
    private Date            flagDate;
    
    public QuadTreeNode(double minX, double maxX, double minY, double maxY)
    {
        this(minX, maxX, minY, maxY, null);
    }
    
    public QuadTreeNode(double minX, double maxX, double minY, double maxY, QuadTreeNode parent)
    {
        if (parent != null)
        {
            this.parent = parent;
            this.depth = parent.depth + 1;
        }
        else
        {
            this.depth = 0;
            this.parent = null;
        }
        
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
        
        this.children = null;
        this.positionInParent = PositionInParent.Root;
        
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
        
        String positionInParentString = nodeElement.getAttributeValue("positionInParent");
        
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
            for (int i = 0; i < 4; ++i)
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
                    
                    children[position.ordinal()] = new QuadTreeNode(minX, maxX, minY, maxY, this);
                    children[position.ordinal()].parent = this;
                    children[position.ordinal()].children = null;
                    children[position.ordinal()].status = Status.VOID;
                    children[position.ordinal()].min = -1;
                    children[position.ordinal()].max = -1;
                    
                    children[position.ordinal()].positionInParent = position;
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
        double[] array = borderPointsAsDouble(pointsPerSide);
        
        for (int i = 0; i < array.length / 2; ++i)
        {
            double real = array[2 * i];
            double img = array[2 * i + 1];
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
    
    private double[] borderPointsAsDouble(int pointsPerSide)
    {
        double minX = this.minX;
        double maxX = this.maxX;
        double minY = this.minY;
        double maxY = this.maxY;
        
        double[] destArray = new double[pointsPerSide * 8];
        
        double step = (maxX - minX) / (double) (pointsPerSide - 1);
        
        // bottom side of the rectangle
        int baseIndex = 0;
        for (int i = 0; i < pointsPerSide; i++)
        {
            destArray[baseIndex + 2 * i] = (minX + (double) i * step);
            destArray[baseIndex + 2 * i + 1] = (minY);
        }
        baseIndex += 2 * pointsPerSide;
        
        // top side of the rectangle
        for (int i = 0; i < pointsPerSide; ++i)
        {
            destArray[baseIndex + 2 * i] = (minX + (double) i * step);
            destArray[baseIndex + 2 * i + 1] = (maxY);
        }
        baseIndex += 2 * pointsPerSide;
        
        // left side of the rectangle
        for (int i = 0; i < pointsPerSide; ++i)
        {
            destArray[baseIndex + 2 * i] = (minX);
            destArray[baseIndex + 2 * i + 1] = (minY + (double) i * step);
        }
        baseIndex += 2 * pointsPerSide;
        
        // bottom side of the rectangle
        for (int i = 0; i < pointsPerSide; ++i)
        {
            destArray[baseIndex + 2 * i] = (maxX);
            destArray[baseIndex + 2 * i + 1] = (minY + (double) i * step);
        }
        
        return destArray;
    }
    
    public void testOutsideMandelbrotSet(int pointsPerSide, int maxIter, int diffIterLimit)
    {
        double[] array = innerPointsAsDouble(pointsPerSide);
        int min, max;
        // init min and max iter
        {
            double real = array[0];
            double img = array[1];
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
        
        for (int i = 0; i < array.length / 2; ++i)
        {
            double real = array[2 * i];
            double img = array[2 * i + 1];
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
        
        this.status = Status.OUTSIDE;
        this.min = min;
        this.max = max;
    }
    
    private double[] innerPointsAsDouble(int pointsPerSide)
    {
        double minX = this.minX;
        double maxX = this.maxX;
        double minY = this.minY;
        double maxY = this.maxY;
        
        double stepX = (maxX - minX) / (double) (pointsPerSide - 1);
        double stepY = (maxY - minY) / (double) (pointsPerSide - 1);
        
        double[] array = new double[pointsPerSide * pointsPerSide * 2];
        
        // bottom side of the rectangle
        for (int i = 0; i < pointsPerSide; i++)
        {
            int base = i * pointsPerSide;
            double xVal = minX + (double) i * stepX;
            for (int j = 0; j < pointsPerSide; ++j)
            {
                array[2 * (base + j)] = xVal;
                array[2 * (base + j) + 1] = (minY + (double) j * stepY);
            }
        }
        
        return array;
    }
    
    public void computeStatus(int pointsPerSide, int maxIter, int diffIterLimit)
    {
        this.testInsideMandelbrotSet(pointsPerSide, maxIter);
        // System.out.println("After inside test " + this.status);
        if (this.status != Status.INSIDE)
        {
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
        
        thisNode.addAttribute(new Attribute("positionInParent", positionInParent.toString()));
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
            flagDate = new Date();
        }
    }
    
    public boolean isFlagedForComputing()
    {
        return flagedForComputing;
    }
    
    public Date getFlagDate()
    {
        return flagDate;
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
}
