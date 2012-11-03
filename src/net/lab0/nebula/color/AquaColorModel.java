
package net.lab0.nebula.color;


public class AquaColorModel
implements ColorationModel
{
    
    @Override
    public void computeColorForPoints(float[] vector, PointValues... values)
    {
        float red = 0;
        float green = 0;
        float blue = 0;
        double x = Math.log(256d) / 255d;
        
        int count = 0;
        PointValues v = values[0];
        float tmp = 255f * (v.value - v.minIter) / (v.maxIter - v.minIter + 1);
        red += Math.exp(tmp * x) - 1;
        green += tmp;
        blue += Math.sqrt(255f * tmp);
        count++;
        
        vector[0] = red / (float) count;
        vector[1] = green / (float) count;
        vector[2] = blue / (float) count;
    }
    
    @Override
    public int getChannelsCount()
    {
        return 1;
    }
    
}
