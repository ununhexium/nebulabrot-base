
package net.lab0.nebula.color;

/**
 * gray 
 * 
 * @author 116
 *
 */
public class GrayScaleColorModel
implements ColorationModel
{
    @Override
    public void computeColorForPoint(float[] vector, PointValues... values)
    {
        float rgb = 0;
        PointValues v = values[0];
        rgb = 255f * (v.value - v.minIter) / (v.maxIter - v.minIter + 1);
        vector[0] = vector[1] = vector[2] = rgb;
    }

    @Override
    public int getChannelsCount()
    {
        return 1;
    }
}
