package net.lab0.nebula.color;

/**
 * A natural logarithmic gray coloration model.
 * 
 * @author 116@lab0.net
 * 
 */
public class LogGrayScaleColorModel
implements ColorationModel
{
    
    @Override
    public void computeColorForPoint(float[] vector, PointValues... values)
    {
        PointValues v = values[0];
        // the minimal acceptable value for the log function
        long minInput = v.value + 1;
        // maximum possible output value
        double maxOutput = Math.log(v.maxIter);
        // minimum possible output value
        double minOuput = Math.log(minInput);
        // output (range ]0;1]) this the ratio between the 2 outputs
        double output = minOuput / maxOutput;
        // converting to a 255f max float value
        float rgb = (float) output * 255f;
        vector[0] = vector[1] = vector[2] = rgb;
    }
    
    @Override
    public int getChannelsCount()
    {
        return 1;
    }
    
}
