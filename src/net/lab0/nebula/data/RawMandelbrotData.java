
package net.lab0.nebula.data;


import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.Serializable;

import net.lab0.nebula.color.ColorationModel;
import net.lab0.nebula.color.GrayScaleColorationModel;
import net.lab0.nebula.color.PointValues;


/**
 * 
 * Contains the raw computation of a mandelbrot / nebulabrot set
 * 
 * @author 116
 * 
 */
public class RawMandelbrotData
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private int               pixelWidth;
    private int               pixelHeight;
    private int[][]           data;
    
    public RawMandelbrotData(int pixelWidth, int pixelHeight)
    {
        super();
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        data = new int[pixelWidth][pixelHeight];
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
    
    public BufferedImage computeBufferedImage(ColorationModel colorationModel)
    {
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final BufferedImage bufferedImage = gc.createCompatibleImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_RGB);
        
        // System.out.println("" + raw.getPixelWidth() + " * " + raw.getPixelHeight());
        int min = data[0][0];
        int max = data[0][0];
        for (int x = 0; x < pixelWidth; ++x)
        {
            for (int y = 0; y < pixelHeight; ++y)
            {
                if (data[x][y] > max)
                {
                    max = data[x][y];
                }
                if (data[x][y] < min)
                {
                    min = data[x][y];
                }
            }
        }
        
        WritableRaster raster = bufferedImage.getRaster();
        float[] fArray = new float[3];
        PointValues value = new PointValues();
        value.minIter = min;
        value.maxIter = max;
        for (int x = 0; x < pixelWidth; ++x)
        {
            for (int y = 0; y < pixelHeight; ++y)
            {
                value.value = data[x][y];
                colorationModel.computeColorForPoints(fArray, value);
                raster.setPixel(x, y, fArray);
            }
        }
        
        return bufferedImage;
    }
    
}
