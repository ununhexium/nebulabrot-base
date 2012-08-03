
package net.lab0.nebula.data;


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
    
    public PositionInParent positionInParent;
    public Status           status;
    public int              min = -1;
    public int              max = -1;
    
    public QuadTreeNode(double minX, double maxX, double minY, double maxY)
    {
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
        
        this.children = null;
        this.parent = null;
        this.positionInParent = PositionInParent.Root;
        
        this.status = Status.VOID;
    }
    
    public int getNodeDepth()
    {
        if (parent == null)
        {
            return 0;
        }
        else
        {
            return 1 + parent.getNodeDepth();
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
                if (position.equals(PositionInParent.TopLeft) || position.equals(PositionInParent.TopRight) || position.equals(PositionInParent.BottomLeft)
                || position.equals(PositionInParent.BottomRight))
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
                    
                    children[position.ordinal()] = new QuadTreeNode(minX, maxX, minY, maxY);
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
            
//            System.out.println("" + real + "+i" + img + " " + iter);
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
            
//            System.out.println("" + real + "+i" + img + " iter=" + iter + " diff=" + (max - min + 1));
            
            if ((max - min + 1) > diffIterLimit)
            {
                this.status = Status.BROWSED;
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
//        System.out.println("After inside test " + this.status);
        if (this.status != Status.INSIDE)
        {
            this.testOutsideMandelbrotSet(pointsPerSide, maxIter, diffIterLimit);
//            System.out.println("After outside test " + this.status);
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
}
