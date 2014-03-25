package net.lab0.nebula.color;

import org.junit.Assert;
import org.junit.Test;

public class TestGrayScaleColorModel
{
    private GrayScaleColorModel model = new GrayScaleColorModel();
    
    @Test
    public void testComputeColorForPoint1()
    {
        PointValues values = new PointValues();
        values.minIter = 123;
        values.maxIter = 687;
        values.value = 123;
        
        float[] vector = new float[3];
        model.computeColorForPoint(vector, values);
        Assert.assertEquals(0f, vector[0], 0.1f);
        Assert.assertEquals(0f, vector[1], 0.1f);
        Assert.assertEquals(0f, vector[2], 0.1f);
    }
    
    @Test
    public void testComputeColorForPoint2()
    {
        PointValues values = new PointValues();
        values.minIter = 123;
        values.maxIter = 6870;
        values.value = 6870;
        
        float[] vector = new float[3];
        model.computeColorForPoint(vector, values);
        Assert.assertEquals(255f, vector[0], 0.1f);
        Assert.assertEquals(255f, vector[1], 0.1f);
        Assert.assertEquals(255f, vector[2], 0.1f);
    }
    
    @Test
    public void testComputeColorForPoint3()
    {
        PointValues values = new PointValues();
        values.minIter = 123;
        values.maxIter = 687;
        values.value = 500;
        
        float expected = 255f* ((float) (500-123) / (687 - 123));
        
        float[] vector = new float[3];
        model.computeColorForPoint(vector, values);
        Assert.assertEquals(expected, vector[0], 1.0f);
        Assert.assertEquals(expected, vector[1], 1.0f);
        Assert.assertEquals(expected, vector[2], 1.0f);
    }
    
    @Test
    /* A bit of a stupid check but you never know, maybe a bad refactor / variable extraction will make this fail. */
    public void testChannelCount()
    {
        Assert.assertEquals(1, model.getChannelsCount());
    }
}
