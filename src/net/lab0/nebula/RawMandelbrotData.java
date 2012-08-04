
package net.lab0.nebula;


import java.io.Serializable;


public class RawMandelbrotData
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private int pixelWidth;
    private int pixelHeight;
    private int[][] data;
    
    public RawMandelbrotData(int pixelWidth, int pixelHeight)
    {
        super();
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        data = new int[pixelHeight][pixelHeight];
    }
    
    public int getPixelWidth()
    {
        return pixelWidth;
    }
    
    public int getPixelHeight()
    {
        return pixelHeight;
    }
    
    public int[][] getData()
    {
        return data;
    }
    
}
