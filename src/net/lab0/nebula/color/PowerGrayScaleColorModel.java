package net.lab0.nebula.color;

/**
 * 
 * Gray with exponentiated scale. Makes intermediates zones [1-254] brighter than the @See GrayScaleColorModel if the
 * 
 * @author 116@lab0.net
 * 
 */
public class PowerGrayScaleColorModel
implements ColorationModel
{
    private double power = 1.0;
    
    public PowerGrayScaleColorModel(double power)
    {
        this.power = power;
    }
    
    @Override
    public void computeColorForPoint(float[] vector, PointValues... values)
    {
        /**
         * <pre>
         * 
         * note:
         * powerX(A) = A to the power X, A^X
         * 
         * we want:
         * min value -> 0
         * max value -> 255
         * 
         * this means that we must have:
         *     powerX(min) = 0
         *     powerX(max) = 255
         * 
         * so we must do:
         *     f(x)=powerX(x-min) -> 0 if x=min
         * but we must also have:
         *     f(x) -> 255 if x=max
         * 
         * to satisfy that we normalize:
         *     (x-min) / (max-min) -> [0;1]
         *     powerX((x-min)/(max-min)) -> [0;1] because input value in [0;1]
         *     
         * special case: min=max -> add+1 in order not to divide by zero
         *     powerX((x-min)/(max-min) +1) -> [0-0.9999...]
         *     
         * conversion to 0-255 range -> above expression * 255f
         * 
         * </pre>
         */

        PointValues v = values[0];
        //warning : do not change this 1.0d or this will become an int divison and it will always be 0
        float rgb = (float) (255f * Math.pow((v.value - v.minIter) / (v.maxIter - v.minIter + 1.0d), power));
        vector[0] = vector[1] = vector[2] = rgb;
    }
    
    @Override
    public int getChannelsCount()
    {
        return 1;
    }
}
