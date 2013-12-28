package net.lab0.nebula.data;

import org.junit.Assert;
import org.junit.Test;

public class TestPointsBlock
{
    @Test
    public void testPointsBlock1()
    {
        int size = 5;
        PointsBlock p = new PointsBlock(size);
        Assert.assertEquals(size, p.size);
        Assert.assertNotNull(p.imag);
        Assert.assertNotNull(p.iter);
        Assert.assertNotNull(p.real);
        Assert.assertEquals(size, p.imag.length);
        Assert.assertEquals(size, p.iter.length);
        Assert.assertEquals(size, p.real.length);
    }
    
    @Test
    public void testPointsBlock2()
    {
        int size = 5;
        double[] real = new double[size];
        double[] imag = new double[size];
        
        PointsBlock p = new PointsBlock(size, real, imag);
        Assert.assertEquals(size, p.size);
        Assert.assertEquals(imag, p.imag);
        Assert.assertNotNull(p.iter);
        Assert.assertEquals(real, p.real);
        Assert.assertEquals(size, p.imag.length);
        Assert.assertEquals(size, p.iter.length);
        Assert.assertEquals(size, p.real.length);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPointsBlock3()
    {
        int size = 5;
        double[] real = new double[size - 1];
        double[] imag = new double[size + 1];
        
        new PointsBlock(size, real, imag);
    }
    
    @Test
    public void testReset()
    {
        int size = 10;
        PointsBlock p = new PointsBlock(size);
        for (int i = 0; i < size; ++i)
        {
            p.real[i] = 1.0;
            p.imag[i] = 1.0;
            p.iter[i] = 1;
        }
        
        p.reset();
        for (int i = 0; i < size; ++i)
        {
            Assert.assertEquals(0.0, p.real[i], 0);
            Assert.assertEquals(0.0, p.imag[i], 0);
            Assert.assertEquals(0, p.iter[i]);
        }
    }
}
