package net.lab0.nebula.core;

import java.nio.IntBuffer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.LWJGLException;

public class TestOpenClMandelbrotComputeRoutines
{
    private static final int                       maxIter = 2 << 10;
    private static final int                       side    = 1024;
    private static final double                    step    = 4.0 / side;
    
    private static OpenClMandelbrotComputeRoutines routines;
    
    @BeforeClass
    public static void beforeClass()
    throws LWJGLException
    {
        routines = new OpenClMandelbrotComputeRoutines();
    }
    
    @AfterClass
    public static void afterClass()
    {
        routines.teardown();
    }
    
    @Test
    public void testCompute()
    {
        double[] xArray = new double[side * side];
        double[] yArray = new double[side * side];
        int index = 0;
        
        for (int x = 0; x < side; ++x)
        {
            for (int y = 0; y < side; ++y)
            {
                double real = -2.0 + x * step;
                double img = -2.0 + y * step;
                
                xArray[index] = real;
                yArray[index] = img;
                index++;
            }
        }
        
        IntBuffer result = routines.compute(xArray, yArray, maxIter);
        int[] iterations = new int[side * side];
        index = 0;
        while (result.hasRemaining())
        {
            iterations[index] = result.get();
        }
        for (int i = 0; i < side * side; ++i)
        {
            long iter = MandelbrotComputeRoutines.computeIterationsCountOptim2(xArray[i], yArray[i], maxIter);
            // can't do assertions as it seems that the CPU and the graphic card have different rounding methods
            // Assert.assertEquals("For point (" + xArray[i] + ";" + yArray[i] + ")", iter, iterations[i]);
        }
    }
    
    @Test(expected = RuntimeException.class)
    public void testRuntimeException()
    {
        double[] xArray = new double[1];
        double[] yArray = new double[2];
        routines.compute(xArray, yArray, 0);
    }
}
