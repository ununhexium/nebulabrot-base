package net.lab0.nebula.example;

import java.net.URISyntaxException;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;

import net.lab0.nebula.core.MandelbrotComputeRoutines;
import net.lab0.nebula.core.OpenClMandelbrotComputeRoutines;
import net.lab0.tools.HumanReadable;

import org.lwjgl.LWJGLException;

import com.google.common.base.Stopwatch;

/**
 * This example shows how to use the openCL compute routine directly.
 * 
 * @author 116@lab0.net
 * 
 */
public class AdvancedExample1
{
    public static void main(String[] args)
    throws LWJGLException, URISyntaxException
    {
        // the number of points we want to compute on X and Y axis
        int size = 2048;
        int blockSize = 1024 * 1024;
        
        // init openCL computation class
        OpenClMandelbrotComputeRoutines ocl = new OpenClMandelbrotComputeRoutines();
        
        // we are going to time the computation
        Stopwatch stopWatch = Stopwatch.createStarted();
        
        /*
         * counts the number of computed iterations. The time the GPU is going to run is proportional to the number of
         * iterations we compute.
         */
        long totalIterations = 0;
        
        /*
         * Contains the coordinates of the points to compute. This array will be reallocated for each loop.
         */
        double[] xCoordinates = new double[blockSize];
        double[] yCoordinates = new double[blockSize];
        /*
         * The step size, the Y origin
         */
        double step = 1.0 / size;
        double yCurrent = -2.0;
        // the index of the currently filled item in the x and y array.
        int index = 0;
        // count the number of calls to openCL computing routines
        int passes = 0;
        /*
         * The maximum iteration in the Mandelbrot formula. If you increase this value you will see an even bigger
         * advantage for the GPU.
         */
        int maxIter = 128;
        
        System.out.println("Computing with openCL");
        // compute points in the given area
        for (int y = 0; y < size; ++y)
        {
            yCurrent = -0.5 + step * y;
            for (int x = 0; x < size; ++x)
            {
                // add the coordinates of the current point
                yCoordinates[index] = yCurrent;
                xCoordinates[index] = -0.5 + step * x;
                index++;
                // when we filled a block, we compute it with openCL
                if (index == blockSize)
                {
                    // copies the reference to be able to assign new values
                    System.out.println("Block" + passes);
                    final double[] xCtmp = xCoordinates;
                    final double[] yCtmp = yCoordinates;
                    // reallocate in order not to overwrite the other array
                    // TODO: check if this is really needed
                    xCoordinates = new double[blockSize];
                    yCoordinates = new double[blockSize];
                    
                    IntBuffer result = ocl.compute(xCtmp, yCtmp, maxIter);
                    // System.out.println("Ended computation");
                    result.rewind();
                    long total = 0;
                    /*
                     * retrieve the result: count the number of iterations we did
                     */
                    for (int i = 0; i < result.capacity(); ++i)
                    {
                        total += result.get();
                    }
                    totalIterations += total;
                    
                    index = 0;
                    passes++;
                }
            }
        }
        
        System.out.println("Passes: " + passes);
        
        stopWatch.stop();
        System.out.println("OpenCL computation: " + stopWatch.toString());
        
        long speed = totalIterations / stopWatch.elapsed(TimeUnit.MILLISECONDS) * 1000; // iterations per second
        System.out.println(HumanReadable.humanReadableNumber(speed, true) + " CL iteration per second");
        
        // release the resources
        ocl.teardown();
        
        System.out.println("Computing with the CPU (single thread)");
        // optional because the CPU is really slow
        if (args.length > 0 && "opt".equals(args[0]))
        {
            yCurrent = -2.0;
            stopWatch.reset();
            stopWatch.start();
            totalIterations = 0;
            for (int y = 0; y < size; ++y)
            {
                yCurrent = -0.5 + step * y;
                for (int x = 0; x < size; ++x)
                {
                    // huh coco !
                    totalIterations += MandelbrotComputeRoutines.computeIterationsCountOptim2(-0.5 + step * x,
                    yCurrent, maxIter);
                }
            }
            
            stopWatch.stop();
            System.out.println("CPU computation: " + stopWatch.toString());
            
            speed = totalIterations / stopWatch.elapsedTime(TimeUnit.MILLISECONDS) * 1000; // points per second
            System.out.println(HumanReadable.humanReadableNumber(speed, true) + " CPU iteration per second");
        }
    }
}
