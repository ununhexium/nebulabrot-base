
package net.lab0.nebula;


import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


@SuppressWarnings("serial")
public class DisplayFrame
extends JFrame
{
    private RawMandelbrotData rawMandelbrotData;
    
    public DisplayFrame(RawMandelbrotData rawMandelbrotData)
    {
        this.rawMandelbrotData = rawMandelbrotData;
        
        this.setSize(this.rawMandelbrotData.getPixelWidth(), this.rawMandelbrotData.getPixelHeight());
        
        BufferedImage bufferedImage = createCompatibleImage(this.rawMandelbrotData.getPixelWidth(), this.rawMandelbrotData.getPixelHeight(),
        BufferedImage.TYPE_INT_RGB);
        
        long[][] data = this.rawMandelbrotData.getData();
        long min = data[0][0];
        long max = data[0][0];
        for (int x = 0; x < this.rawMandelbrotData.getPixelWidth(); ++x)
        {
            for (int y = 0; y < this.rawMandelbrotData.getPixelHeight(); ++y)
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
        
        System.out.println("min/max=" + min + "/" + max);
        
        long gap = max - min + 1;
        WritableRaster raster = bufferedImage.getRaster();
        for (int x = 0; x < this.rawMandelbrotData.getPixelWidth(); ++x)
        {
            for (int y = 0; y < this.rawMandelbrotData.getPixelHeight(); ++y)
            {
                float[] fArray = new float[3];
                fArray[0] = fArray[1] = fArray[2] = 255 * (data[x][y] - min) / gap;
                
                raster.setPixel(x, y, fArray);
            }
        }
        
        ImageIcon icon = new ImageIcon(bufferedImage);
        this.add(new JLabel(icon));
     
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    
    private static BufferedImage createCompatibleImage(int width, int height, int type)
    {
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        
        return gc.createCompatibleImage(width, height, type);
    }
}
