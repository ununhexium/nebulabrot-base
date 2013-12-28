package net.lab0.nebula.color;

/**
 * A linear gray scale coloration. Works with 1 channel.
 * 
 * @see ColorationModel for details.
 * 
 * @since 1.0
 * @author 116@lab0.net
 * 
 */
public class GrayScaleColorModel
implements ColorationModel
{
    @Override
    public void computeColorForPoint(float[] vector, PointValues... values)
    {
        /**
         * <pre>
         * 
         * we want:
         * min value -> 0
         * max value -> 255
         * 
         * this means that we must have:
         *     f(min) = 0
         *     f(max) = 255
         * 
         * so we must do:
         *     f(x)= x-min -> 0 if x=min
         * but we must also have:
         *     f(x) -> 255 if x=max
         * 
         * to satisfy this condition we normalize:
         *     (x-min) / (max-min) -> [0;1]
         * 
         * special case: min=max -> add +1 in order not to divide by zero
         *     (x-min)/((max-min) +1) -> [0-0.9999...]
         *     
         * conversion to 0-255 range -> above expression * 255f
         * 
         * </pre>
         */
        
        float rgb = 0;
        PointValues v = values[0];
        // note : it is important to start with the *255f in order to avoid the integer division that would always
        // return 0
        rgb = 255f * (v.value - v.minIter) / (v.maxIter - v.minIter + 1);
        vector[0] = vector[1] = vector[2] = rgb;
    }
    
    @Override
    public int getChannelsCount()
    {
        return 1;
    }
}
