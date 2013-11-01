package net.lab0.nebula.core;

import java.nio.IntBuffer;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.lwjgl.LWJGLException;

import net.lab0.nebula.core.MandelbrotComputeRoutines;
import net.lab0.nebula.core.OpenClMandelbrotComputeRoutines;

public class TestOpenClMandelbrotComputeRoutines
{
    private static final int                       maxIter = 2 << 10;
    private static final int                       side    = 1024;
    private static final double                    step    = 4.0 / side;
    
    private static OpenClMandelbrotComputeRoutines routines;
    
    @BeforeClass
    public static void beforeClass()
    {
        try
        {
            routines = new OpenClMandelbrotComputeRoutines();
        }
        catch (LWJGLException e)
        {
            Assert.fail("Failed to init the OpenCL computing class.");
        }
    }
    
    @Ignore //TODO: why ?
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
            int iter = MandelbrotComputeRoutines.computeIterationsCountOptim2(xArray[i], yArray[i], maxIter);
            Assert.assertEquals("For point (" + xArray[i] + ";" + yArray[i] + ")", iter, iterations[i]);
        }
    }
}
