package net.lab0.nebula.test;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.lwjgl.LWJGLException;

import net.lab0.nebula.core.OpenClMandelbrotComputeRoutines;

public class TestOpenClMandelbrotComputeRoutines
{
    private OpenClMandelbrotComputeRoutines routines;
    
    @BeforeClass
    public void setup()
    {
        try
        {
            routines = new OpenClMandelbrotComputeRoutines();
        }
        catch (LWJGLException e)
        {
            Assert.fail();
            Assert.fail("Failed to init the OpenCL computing class.");
        }
    }
    
    public void testLoadText(){
        String text = routines.loadText("./cl/referenceForTests.cl");
        String reference = "kernel void mandelbrot(global const double* a, global const double* b, global int* result, int const size, int const maxIter){    const int itemId = get_global_id(0);    if(itemId < size)    {        ...    }}";
        Assert.assertEquals("Read text doesn't match the reference.", reference, text);
    }
}
