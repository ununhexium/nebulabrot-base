
package net.lab0.nebula.color;


public class GrayScaleColorationModel
implements ColorationModel
{
    @Override
    public void computeColorForPoints(float[] vector, PointValues... values)
    {
        float rgb = 0;
        
        int count = 0;
        for (PointValues v : values)
        {
            rgb += 255f * (v.value - v.minIter) / (v.maxIter - v.minIter + 1);
            count++;
        }
        vector[0] = vector[1] = vector[2] = rgb / (float) count;
    }
}
