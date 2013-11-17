package net.lab0.nebula.core;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

public class TestBigMandelbrotComputeRoutines
{
    @Test
    public void testIsInsineMandelbrotSet()
    {
        BigDecimal val1 = BigDecimal.ZERO;
        BigDecimal val2 = BigDecimal.ZERO;
        long result = 0;
        
        result = BigMandelbrotComputeRoutines.computeIterationsCount(val1, val2, 100, 16);
        Assert.assertEquals(100, result);
        
        result = BigMandelbrotComputeRoutines.computeIterationsCount(val1, val2, 65536, 16);
        Assert.assertEquals(65536, result);
        
        val1 = new BigDecimal("2.0");
        val2 = new BigDecimal("2.0");
        result = BigMandelbrotComputeRoutines.computeIterationsCount(val1, val2, 65536, 16);
        Assert.assertEquals(0, result);
        
        // val1 = new BigDecimal("0.00001");
        // val2 = new BigDecimal("0.00000");
        // result = BigMandelbrotComputeRoutines.computeIterationsCount(val1, val2, 65536, 16);
    }
    
    // TODO: test precision difference
}
