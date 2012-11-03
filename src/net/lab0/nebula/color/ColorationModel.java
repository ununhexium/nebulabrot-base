
package net.lab0.nebula.color;




public interface ColorationModel
{
    public void computeColorForPoints(float[] vector, PointValues... values); // float[3]
    
    /**
     * 
     * @return the number of channels this coloration model needs to work
     */
    public int getChannelsCount();
}
