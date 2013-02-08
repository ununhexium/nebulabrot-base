
package net.lab0.nebula.color;

/**
 * 
 * Blue and turquoise coloration.
 * 
 * @author 116
 *
 */
public class AquaColorModel
implements ColorationModel
{
    
    @Override
    public void computeColorForPoint(float[] vector, PointValues... values)
    {
        float red = 0;
        float green = 0;
        float blue = 0;
        double x = Math.log(256d) / 255d;
        
        PointValues v = values[0];
        float tmp = 255f * (v.value - v.minIter) / (v.maxIter - v.minIter + 1);
        red = (float) (Math.exp(tmp * x) - 1);
        green = tmp;
        blue = (float) Math.sqrt(255f * tmp);
        
        vector[0] = red;
        vector[1] = green;
        vector[2] = blue;
    }
    
    @Override
    public int getChannelsCount()
    {
        return 1;
    }
    
}
