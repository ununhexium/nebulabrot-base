package net.lab0.nebula.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.lab0.nebula.core.MandelbrotComputeRoutines;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMandelbrotComputeRoutines
{
    private static int        maxIter         = 2 << 10;
    private static int        side            = 1024;
    private static double     step            = 4.0 / side;
    private static int        timersLoopCount = 4;
    
    // optim1 timers > optim2 timers >= optim4 timers
    private static List<Long> optim1Times     = new ArrayList<>();
    private static List<Long> optim2Times     = new ArrayList<>();
    private static List<Long> optim4Times     = new ArrayList<>();
    
    @BeforeClass
    public static void testIsOutsideMandelbrotSetReference()
    {
        // not much to test here as it is the reference function
    }
    
    @Test
    public void testIsOutsideMandelbrotResults()
    {
        for (int x = 0; x < side; ++x)
        {
            for (int y = 0; y < side; ++y)
            {
                double real = -2.0 + x * step;
                double img = -2.0 + y * step;
                assertEquals(MandelbrotComputeRoutines.isOutsideMandelbrotSetReference(real, img, maxIter),
                MandelbrotComputeRoutines.isOutsideMandelbrotSet(real, img, maxIter));
            }
        }
    }
    
    @Test
    public void testIsOutsideMandelbrotSetExecTime()
    {
        StopWatch stopWatch = new StopWatch();
        for (int i = 0; i < timersLoopCount; ++i)
        {
            stopWatch.start();
            for (int x = 0; x < side; ++x)
            {
                for (int y = 0; y < side; ++y)
                {
                    double real = -2.0 + x * step;
                    double img = -2.0 + y * step;
                    MandelbrotComputeRoutines.isOutsideMandelbrotSet(real, img, maxIter);
                }
            }
            stopWatch.stop();
            optim1Times.add(stopWatch.getNanoTime());
            stopWatch.reset();
        }
    }
    
    @Test
    public void testIsOutsideMandelbrotOptim2Results()
    {
        for (int x = 0; x < side; ++x)
        {
            for (int y = 0; y < side; ++y)
            {
                double real = -2.0 + x * step;
                double img = -2.0 + y * step;
                assertEquals(MandelbrotComputeRoutines.isOutsideMandelbrotSetReference(real, img, maxIter),
                MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim2(real, img, maxIter));
            }
        }
    }
    
    @Test
    public void testIsOutsideMandelbrotOptim2SetExecTime()
    {
        StopWatch stopWatch = new StopWatch();
        for (int i = 0; i < timersLoopCount; ++i)
        {
            stopWatch.start();
            for (int x = 0; x < side; ++x)
            {
                for (int y = 0; y < side; ++y)
                {
                    double real = -2.0 + x * step;
                    double img = -2.0 + y * step;
                    MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim2(real, img, maxIter);
                }
            }
            stopWatch.stop();
            optim2Times.add(stopWatch.getNanoTime());
            stopWatch.reset();
        }
    }
    
    @Test
    public void testIsOutsideMandelbrotOptim4Results()
    {
        for (int x = 0; x < side; ++x)
        {
            for (int y = 0; y < side; ++y)
            {
                double real = -2.0 + x * step;
                double img = -2.0 + y * step;
                assertEquals(MandelbrotComputeRoutines.isOutsideMandelbrotSetReference(real, img, maxIter),
                MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim4(real, img, maxIter));
            }
        }
    }
    
    @Test
    public void testIsOutsideMandelbrotOptim4SetExecTime()
    {
        StopWatch stopWatch = new StopWatch();
        for (int i = 0; i < timersLoopCount; ++i)
        {
            stopWatch.start();
            for (int x = 0; x < side; ++x)
            {
                for (int y = 0; y < side; ++y)
                {
                    double real = -2.0 + x * step;
                    double img = -2.0 + y * step;
                    MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim4(real, img, maxIter);
                }
            }
            stopWatch.stop();
            optim4Times.add(stopWatch.getNanoTime());
            stopWatch.reset();
        }
    }
    
    @AfterClass
    public static void testTimers()
    {
        long total1 = 0;
        long total2 = 0;
        long total4 = 0;
        
        for (int i = 0; i < timersLoopCount; ++i)
        {
            total1 += optim1Times.get(i);
            total2 += optim2Times.get(i);
            total4 += optim4Times.get(i);
        }
        
        Assert.assertTrue(total1 > total2);
        Assert.assertEquals((double) total2, (double) total4, total2 * 0.05);// these 2 must be about the same speed: 5% gape tolerance
    }
}
