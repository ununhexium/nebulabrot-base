package net.lab0.nebula.color;

public class LogGrayScaleColorModel implements ColorationModel
{

    @Override
    public void computeColorForPoint(float[] vector, PointValues... values)
    {
        PointValues v = values[0];
        //warning : do not change this 1.0d or this will become an int divison and it will always be 0
        double var = Math.log(v.value + 1) / Math.log(v.maxIter);
        float rgb = (float) var * 255f;
        vector[0] = vector[1] = vector[2] = rgb;
    }

    @Override
    public int getChannelsCount()
    {
        return 1;
    }
    
}
